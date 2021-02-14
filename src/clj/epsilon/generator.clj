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
           [java.io File]))

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
     :problems? (if (.isEmpty parse-problems)
                  false
                  parse-problems)}))

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

(defn generate
  "Generate an EGX file with the given XML model. Return the EGX path or exception if exists."
  ([egx-path model-paths output-dir-path]
   (let [egx-module (->egx-module egx-path output-dir-path)]
     (if-let [problems (:problems egx-module)]
       (throw (ex-info "Parse problem found" {:problems problems}))
       (let [egx-module (:module egx-module)
             xml-models (map model-path->xml model-paths)]
         (doall (map #(-> egx-module .getContext .getModelRepository (.addModel %)) xml-models))
         (try
           (log/info "Executing" egx-path)
           (.execute egx-module)
           {:egx-path egx-path}
           (catch EolRuntimeException e
             (throw (ex-info "EOL exception when executed" {:exception e})))))))))

(defmulti file-change-handler (fn [file model-paths output-dir-path] (fs/extension file)))

(defmethod file-change-handler ".egx"
  [file model-paths output-dir-path]
  (log/info file "changed. Regenerating")
  (try
    (generate file model-paths output-dir-path)
    (catch ExceptionInfo e
      (log/error "Exception found when trying to hot-reload" file)
      (.printStackTrace e))))

(defmethod file-change-handler ".egl"
  [file model-paths output-dir-path]
  (log/info file "changed. Regenerating")
  (try
    (let [egx-file-path (replace-ext file "egx")]
      (if (fs/exists? egx-file-path)
        (try
          (generate egx-file-path model-paths output-dir-path)
          (catch ExceptionInfo e
            (log/error "Exception found when trying to hot-reload" file)
            (.printStackTrace e)))))))

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

  Support file creation, deletion and modification."
  [template-dir model-paths output-dir-path]
  ;; By default each file change fires two events, once for the file content and once for the timestamp. Hence we need
  ;; to check the file timestamp so that we only run hot-reload with one unique event so no double reload.
  (let [watcher (DirectoryWatchingUtility/watch (-> template-dir fs/file .toPath)
                                                (fn [f] (true? (some true? ((juxt egl? egx?) f))))
                                                (fn [f] (file-change-handler f model-paths output-dir-path))
                                                (fn [f] (file-change-handler f model-paths output-dir-path))
                                                (fn [f] (file-change-handler f model-paths output-dir-path)))]
    (.watchAsync watcher)
    (fn [] (.close watcher))))

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

(comment
  (def handler (watch "test/resources/templates"
                      ["test/resources/templates/library.xml"]
                      "test/resources/actual/watch_test")))