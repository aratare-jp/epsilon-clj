(ns epsilon.generator
  (:require [puget.printer :refer [pprint]]
            [me.raynes.fs :as fs]
            [taoensso.timbre :as log]
            [epsilon.utility :refer :all]
            [medley.core :as m]
            [clojure.java.io :as io])
  (:import [org.eclipse.epsilon.egl EgxModule]
           [org.eclipse.epsilon.emc.plainxml PlainXmlModel]
           [epsilon CustomEglFileGeneratingTemplateFactory DirectoryWatchingUtility]
           [org.eclipse.epsilon.evl EvlModule]
           [org.eclipse.epsilon.egl.internal EglModule]))

(defn path->xml
  "Load the model at the path and convert it to PlainXmlModel."
  [path]
  (doto (new PlainXmlModel)
    (.setFile (fs/file path))
    (.setName (fs/name path))
    (.load)))

(defn ->template-factory
  "Return a new template factory that outputs to the given path."
  [path ops]
  (doto (new CustomEglFileGeneratingTemplateFactory)
    (.setOutputRoot path)
    (.addOperations ops)))

(defmulti ->epsilon-module
  "Given a path, convert it into the appropriate module based on its extension."
  (fn [path & _] (fs/extension path)))

(defmethod ->epsilon-module ".egl"
  [path]
  (let [egl-file       (fs/file path)
        egl-module     (doto (new EglModule) (.parse egl-file))
        parse-problems (.getParseProblems egl-module)]
    (if (empty? parse-problems)
      egl-module
      (throw (ex-info "Parsed problems found" {:payload parse-problems})))))

(defmethod ->epsilon-module ".egx"
  [path output-path]
  (let [ops            (log/spy :info (-> "protected.egl" io/resource fs/file ->epsilon-module .getOperations))
        factory        (->template-factory output-path ops)
        egx-file       (fs/file path)
        egx-module     (doto (new EgxModule factory) (.parse egx-file))
        parse-problems (.getParseProblems egx-module)]
    (if (empty? parse-problems)
      egx-module
      (throw (ex-info "Parsed problems found" {:payload parse-problems})))))

(defmethod ->epsilon-module ".evl"
  [path]
  (let [evl-file       (fs/file path)
        evl-module     (doto (new EvlModule) (.parse evl-file))
        parse-problems (.getParseProblems evl-module)]
    (if (empty? parse-problems)
      evl-module
      (throw (ex-info "Parsed problems found" {:payload parse-problems})))))

(defn path->epsilon-files
  "Return all files that satisfy any of the given types within the given directory.

  Accept eol?, egl?, egx? and evl? or any combinations. Ignore invalid functions."
  ([path types]
   (path->epsilon-files path types true))
  ([path types recursive?]
   (let [types     (filter #{eol? egl? egx? evl?} types)
         filter-fn #(->> % ((apply juxt types)) ((partial some true?)))]
     (if recursive?
       (m/join (fs/walk (fn [root _ files] (->> files (filter filter-fn) (map #(fs/file root %)))) path))
       (->> path (fs/list-dir) (filter fs/file?) (filter filter-fn))))))

(defn execute
  "Execute an EOL file with the given XML models."
  ([model-paths path & args]
   (let [xml-models (doall (map #(path->xml %) model-paths))]
     (doto (apply ->epsilon-module path args)
       (-> .getContext .getModelRepository .getModels (.addAll xml-models))
       (.execute)))))

(defn generate
  "Generate an EGX file with the given XML models."
  [egx-path model-paths output-path]
  (log/info "Generating" egx-path)
  (execute model-paths egx-path output-path))

(defn validate
  "Validate an EVL file with the given XML models."
  [evl-path model-paths]
  (let [module      (execute model-paths evl-path)
        constraints (-> module .getContext .getUnsatisfiedConstraints)]
    (if (empty? constraints)
      (do
        (log/info "No violation found for" evl-path)
        module)
      (throw (ex-info "Constraints violation were found." {:payload constraints})))))

(defmulti file-change-handler
  "Triggered when a file change. Will dispatch according to what type of file just got changed."
  (fn [file _ _ _] (fs/extension file)))

(defmethod file-change-handler ".egx"
  [file _ model-paths output-path]
  (log/info file "changed. Regenerating.")
  (handle-exception #(generate file model-paths output-path)))

(defmethod file-change-handler ".egl"
  [egl-file _ model-paths output-path]
  (log/info egl-file "changed. Regenerating using the accompanying EGX.")
  (let [egx-path (replace-ext egl-file "egx")]
    (if (fs/exists? egx-path)
      (handle-exception #(generate egx-path model-paths output-path))
      (log/error "Unable to hot-reload because accompanying" egx-path "file is missing."))))

(defmethod file-change-handler ".evl"
  [file _ model-paths _]
  (log/info file "changed. Rerun validation.")
  (handle-exception #(validate (.getAbsolutePath file) model-paths)))

(defmethod file-change-handler ".eol"
  [file _ _ _]
  (log/info file "changed. Regenerating.")
  ;; Nothing yet when EOL is created/modified. One thing we can do is to figure out
  ;; which modules depend on this EOL, then trigger a hot reload on the leaf modules
  ;; since doing so will trigger a down-cascade hot-reload on all relevant dependent
  ;; modules. But meh maybe later.
  (log/warn "No EOL support yet."))

(defmethod file-change-handler ".xml"
  [file template-dir model-paths output-path]
  (log/info "Model" file "changed. Regenerating all.")
  (let [egx-files (path->epsilon-files template-dir [egx?])]
    (handle-exception (fn [] (doall (map #(generate % model-paths output-path) egx-files))))))

(defn watch
  "Watch the given template directory and regenerate if file change is detected.

  Takes a bunch of preds that will filter out which file type to listen to.

  For example, (watch _ _ _ [egl?]) will listen for EGL files only. Can add more as see fit."
  ([template-dir model-paths output-path]
   (watch template-dir model-paths output-path [egl? egx? evl? eol? xml?]))
  ([template-dir model-paths output-path preds]
   (log/info "Watching for file changes. You can now edit files as needed.")
   (let [file-change-handler (fn [f] (file-change-handler f template-dir model-paths output-path))
         watcher             (DirectoryWatchingUtility/watch (-> template-dir fs/file .toPath)
                                                             (fn [f] (true? (some true? ((apply juxt preds) f))))
                                                             file-change-handler
                                                             file-change-handler
                                                             file-change-handler)]
     {:future  (.watchAsync watcher)
      :handler (fn [] (.close watcher))})))

(defn validate-all
  "Go through the provided template directory and validate everything.

  If watch? is true, return the watcher handler to be called to stop the watcher."
  ([{:keys [template-dir model-paths watch?]}]
   (validate-all template-dir model-paths watch?))
  ([template-dir model-paths]
   (validate-all template-dir model-paths true))
  ([template-dir model-paths watch?]
   (let [evl-files   (path->epsilon-files template-dir [evl?])
         evl-modules (doall (map #(validate % model-paths) evl-files))]
     (if watch?
       (watch template-dir model-paths nil [evl? xml?])
       evl-modules))))

(defn generate-all
  "Go through the provided template directory and generate everything.

  If watch? is true, return the watcher handler to be called to stop the watcher."
  ([{:keys [template-dir model-paths output-path watch?]}]
   (generate-all template-dir model-paths output-path watch?))
  ([template-dir model-paths output-path]
   (generate-all template-dir model-paths output-path true))
  ([template-dir model-paths output-path watch?]
   (let [_           (validate-all template-dir model-paths false)
         egx-files   (path->epsilon-files template-dir [egx?])
         egx-modules (doall (map #(generate % model-paths output-path) egx-files))]
     (if watch?
       (watch template-dir model-paths output-path [egx? egl? evl? xml?])
       egx-modules))))

(comment
  (generate "test/resources/templates/generate_test/library.html.egx"
            ["test/resources/templates/generate_test/library.xml"]
            "test/resources/actual"))