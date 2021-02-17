(ns epsilon.generator
  (:require [puget.printer :refer [pprint]]
            [me.raynes.fs :as fs]
            [taoensso.timbre :as log]
            [epsilon.utility :refer :all])
  (:import [org.eclipse.epsilon.egl EgxModule]
           [org.eclipse.epsilon.emc.plainxml PlainXmlModel]
           [org.eclipse.epsilon.eol.exceptions EolRuntimeException]
           [epsilon CustomEglFileGeneratingTemplateFactory DirectoryWatchingUtility]
           [java.io File]
           [org.eclipse.epsilon.evl EvlModule]))

(defn model-path->xml [model-path]
  "Load the model at the path and convert it to PlainXmlModel."
  (try (doto (new PlainXmlModel)
         (.setFile (fs/file model-path))
         (.setName (fs/name model-path))
         (.load))
       (catch Exception e
         {:exception e})))

(defn ->template-factory
  "Return a new EglFileGeneratingTemplateFactory that outputs to the given path."
  [^String output-dir-path]
  (doto (new CustomEglFileGeneratingTemplateFactory) (.setOutputRoot output-dir-path)))

(defmulti ->epsilon-module
          "Given a path, convert it into the appropriate module based on its extension."
          (fn [path & _] (fs/extension path)))

(defmethod ->epsilon-module ".egx"
  [path output-dir-path]
  (let [template-factory (->template-factory output-dir-path)
        ^File egx-file   (fs/file path)
        egx-module       (doto (new EgxModule template-factory) (.parse egx-file))
        parse-problems   (.getParseProblems egx-module)]
    {:module    egx-module
     :problems? (if (.isEmpty parse-problems) false parse-problems)}))

(defmethod ->epsilon-module ".evl"
  [path & _]
  (let [^File egx-file (fs/file path)
        evl-module     (doto (new EvlModule) (.parse egx-file))
        parse-problems (.getParseProblems evl-module)]
    {:module    evl-module
     :problems? (if (.isEmpty parse-problems) false parse-problems)}))

(defn path->epsilon-files
  "Return all files that satisfy any of the given types within the given directory.

  Accept eol?, egl?, egx? and evl? or any combinations. Ignore invalid functions."
  [path recursive? & types]
  (let [types (filter #{eol? egl? egx? evl?} types)]
    (if recursive?
      (let [epsilon-files (fs/walk
                            (fn [root _ files]
                              (->> files
                                   (filter #(->> % ((apply juxt types)) ((partial some true?))))
                                   (map #(.getAbsolutePath (fs/file root %)))))
                            path)]
        (reduce (fn [val new-val] (into val new-val)) '() epsilon-files))
      (->> path
           (fs/list-dir)
           (filter fs/file?)
           (filter #(->> %
                         ((apply juxt types))
                         (reduce (fn [old new] (or old new)))))
           (map #(.getAbsolutePath %))))))

(defn execute
  "Execute an EOL file with the given XML models."
  ([model-paths path & args]
   (if (not (every? #(and (fs/file? %) (xml? %)) model-paths))
     {:exception (ex-info "Model must be a valid XML file." nil)}
     (let [{:keys [problems? module]} (apply ->epsilon-module path args)]
       (if problems?
         (do (doall (for [problem problems?] (log/error problem)))
             {:problems? problems?})
         (let [xml-models (map model-path->xml model-paths)]
           (if (some :exception xml-models)
             (first (filter :exception xml-models))
             (do
               (doall (map #(-> module .getContext .getModelRepository (.addModel %)) xml-models))
               (try
                 (do (.execute module)
                     {:module module})
                 (catch EolRuntimeException e
                   {:exception e}))))))))))

(defn generate
  "Generate an EGX file with the given XML models."
  [egx-path model-paths output-dir-path]
  (log/info "Executing" egx-path)
  (let [{:keys [module problems? exception]} (execute model-paths egx-path output-dir-path)]
    (cond
      problems?
      {:problems? problems?}
      exception
      (do (log/error (.getMessage exception))
          {:exception exception})
      :else
      {:module module})))

(defn validate
  "Validate an EVL file with the given XML models."
  [evl-path model-paths]
  (log/info "Validating" evl-path)
  (let [{:keys [module problems? exception]} (execute model-paths evl-path)]
    (cond
      problems?
      {:problems? problems?}
      exception
      (do (log/error (.getMessage exception))
          {:exception exception})
      :else
      (do (doall (for [constraint (-> module .getContext .getUnsatisfiedConstraints)] (log/error constraint)))
          {:module module}))))

(defmulti file-change-handler
          "Triggered when a file change. Will dispatch according to what type of file just got changed."
          (fn [file _ _] (fs/extension file)))

(defmethod file-change-handler ".egx"
  [file model-paths output-dir-path]
  (log/info file "changed. Regenerating.")
  (generate file model-paths output-dir-path))

(defmethod file-change-handler ".egl"
  [file model-paths output-dir-path]
  (log/info file "changed. Regenerating using the accompanying EGX.")
  (let [egx-file-path (replace-ext file "egx")]
    (if (fs/exists? egx-file-path)
      (generate egx-file-path model-paths output-dir-path)
      (log/error "Unable to hot-reload because accompanying" egx-file-path "file is missing."))))

(defmethod file-change-handler ".evl"
  [file model-paths _]
  (log/info file "changed. Rerun validation.")
  (validate (.getAbsolutePath file) model-paths))

(defmethod file-change-handler ".eol"
  [file model-paths _]
  (log/info file "changed. Regenerating")
  ;; Nothing yet when EOL is created/modified. One thing we can do is to figure out
  ;; which modules depend on this EOL, then trigger a hot reload on the leaf modules
  ;; since doing so will trigger a down-cascade hot-reload on all relevant dependent
  ;; modules. But meh maybe later.
  (log/warn "No EOL support yet."))

(defn watch
  "Watch the given template directory and regenerate if file change is detected.

  Takes a bunch of preds that will filter out which file type to listen to.

  For example, (watch _ _ _ egl?) will listen for EGL files only. Can add more as see fit."
  ([template-dir model-paths output-dir-path]
   (watch template-dir model-paths output-dir-path egl? egx? evl? eol?))
  ([template-dir model-paths output-dir-path & preds]
   ;; By default each file change fires two events, once for the file content and once for the timestamp. Hence we need
   ;; to check the file timestamp so that we only run hot-reload with one unique event so no double reload.
   (let [watcher (DirectoryWatchingUtility/watch (-> template-dir fs/file .toPath)
                                                 (fn [f] (true? (some true? ((apply juxt preds) f))))
                                                 (fn [f] (file-change-handler f model-paths output-dir-path))
                                                 (fn [f] (file-change-handler f model-paths output-dir-path))
                                                 (fn [f] (file-change-handler f model-paths output-dir-path)))]
     {:future  (.watchAsync watcher)
      :handler (fn [] (.close watcher))})))

(defn validate-all
  ([{:keys [template-dir model-paths watch?]}]
   (validate-all template-dir model-paths watch?))
  ([template-dir model-paths watch?]
   (let [evl-files   (path->epsilon-files template-dir true evl?)
         evl-modules (doall (map #(validate % model-paths) evl-files))]
     (if watch?
       {:modules evl-modules
        :watcher (watch template-dir model-paths nil evl?)}
       {:modules evl-modules}))))

(defn generate-all
  "Go through the provided template directory and generate everything.

  If watch? is true, return the watcher handler to be called to stop the watcher."
  ([{:keys [template-dir model-paths output-dir-path watch?]}]
   (generate-all template-dir model-paths output-dir-path watch?))
  ([template-dir model-paths output-dir-path watch?]
   (let [egx-files   (path->epsilon-files template-dir true egx?)
         egx-modules (doall (map #(generate % model-paths output-dir-path) egx-files))]
     (if watch?
       {:modules egx-modules
        :watcher (watch template-dir model-paths output-dir-path)}
       {:modules egx-modules}))))
