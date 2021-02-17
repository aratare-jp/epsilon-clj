(ns epsilon.generator
  (:require [puget.printer :refer [pprint]]
            [me.raynes.fs :as fs]
            [taoensso.timbre :as log]
            [epsilon.utility :refer :all])
  (:import [org.eclipse.epsilon.egl EgxModule]
           [org.eclipse.epsilon.emc.plainxml PlainXmlModel]
           [org.eclipse.epsilon.eol.exceptions EolRuntimeException]
           [epsilon CustomEglFileGeneratingTemplateFactory DirectoryWatchingUtility]
           [clojure.lang ExceptionInfo]
           [java.io File]
           [org.eclipse.epsilon.evl EvlModule]))

(defn model-path->xml [model-path]
  "Load the model at the path and convert it to PlainXmlModel."
  (doto (new PlainXmlModel)
    (.setFile (fs/file model-path))
    (.setName (fs/name model-path))
    (.load)))

(defn ->template-factory
  "Return a new EglFileGeneratingTemplateFactory that outputs to the given path."
  [^String output-dir-path]
  (doto (new CustomEglFileGeneratingTemplateFactory) (.setOutputRoot output-dir-path)))

(defn ->egx-module
  "Take a file path and parses that into an EGX module, which will then be set with the output path.

  Returns a map with keys of :module and :problems? to indicate if there has been parse problems."
  [egx-path output-dir-path]
  (let [template-factory (->template-factory output-dir-path)
        ^File egx-file   (fs/file egx-path)
        egx-module       (doto (new EgxModule template-factory) (.parse egx-file))
        parse-problems   (.getParseProblems egx-module)]
    {:module    egx-module
     :problems? (if (.isEmpty parse-problems) false parse-problems)}))

(defn ->evl-module
  "Take a file path and parses that into an EVL module.

  Returns a map with keys of :module and :problems? to indicate if there has been parse problems."
  [evl-path]
  (let [^File egx-file (fs/file evl-path)
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
  ([eol-module model-paths]
   (let [xml-models (map model-path->xml model-paths)]
     (doall (map #(-> eol-module .getContext .getModelRepository (.addModel %)) xml-models))
     (try
       (do (.execute eol-module)
           {:module eol-module})
       (catch EolRuntimeException e {:exception e})))))

(defn generate
  "Generate an EGX file with the given XML models."
  ([egx-path model-paths output-dir-path]
   (log/info "Executing" egx-path)
   (let [egx-module (->egx-module egx-path output-dir-path)]
     (if-let [problems (:problems? egx-module)]
       (do (doall (for [problem problems] (log/error problem)))
           {:problems? problems})
       (let [egx-module (:module egx-module)
             generated  (execute egx-module model-paths)]
         (if-let [exception (:exception generated)]
           (do (log/error (.getMessage exception))
               {:exception exception})
           {:module egx-module}))))))

(defn validate
  "Validate an EVL file with the given XML models."
  [evl-path model-paths]
  (log/info "Validating" evl-path)
  (let [evl-module (->evl-module evl-path)]
    (if-let [problems (:problems evl-module)]
      (do (doall (for [problem problems] (log/error problem)))
          {:problems problems})
      (let [evl-module (:module evl-module)
            generated  (execute evl-module model-paths)]
        (if-let [exception (:exception generated)]
          (do (log/error (.getMessage exception))
              {:exception exception})
          (do (doall (for [constraint (-> evl-module .getContext .getUnsatisfiedConstraints)]
                       (log/error constraint)))
              {:module evl-module}))))))

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
  [file model-paths output-dir-path]
  (log/info file "changed. Rerun validation.")
  (validate (.getAbsolutePath file) model-paths))

(defmethod file-change-handler ".eol"
  [file model-paths output-dir-path]
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
   (let [evl-files (path->epsilon-files template-dir true evl?)]
     (doall (map #(validate % model-paths) evl-files)))
   (if watch?
     (watch template-dir model-paths nil evl?))))

(defn generate-all
  "Go through the provided template directory and generate everything.

  If watch? is true, return the watcher handler to be called to stop the watcher."
  ([{:keys [template-dir model-paths output-dir-path watch?]}]
   (generate-all template-dir model-paths output-dir-path watch?))
  ([template-dir model-paths output-dir-path watch?]
   (let [egx-files (path->epsilon-files template-dir true egx?)]
     ;; Need to parallelise this somehow. May be store all of the output dirs and then create them before hand.
     (doall (map #(generate % model-paths output-dir-path) egx-files)))
   (if watch?
     (watch template-dir model-paths output-dir-path))))
