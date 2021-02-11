(ns altio.generator
  (:require [puget.printer :refer [pprint]]
            [me.raynes.fs :as fs]
            [hawk.core :as hawk]
            [taoensso.timbre :as log]
            [altio.utility :refer :all])
  (:import [org.eclipse.epsilon.egl EgxModule]
           [org.eclipse.epsilon.emc.plainxml PlainXmlModel]
           [org.eclipse.epsilon.eol.exceptions EolRuntimeException]
           [altio CustomEglFileGeneratingTemplateFactory]
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

(defn path->egx-files
  "Return all egx-files from within the given directory."
  ([path]
   (path->egx-files path true))
  ([path recursive?]
   (if recursive?
     (let [egx-files (fs/walk
                       (fn [root _ files]
                         (->> files
                              (filter #(= (fs/extension %) ".egx"))
                              (mapv #(.getAbsolutePath (fs/file root %)))))
                       path)]
       egx-files
       (reduce (fn [val new-val] (into val new-val)) [] egx-files))
     (-> path
         (fs/list-dir)
         (filter #(and (fs/file? %) (= (fs/extension %) ".egx")))))))

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

(defn create-modify-handler
  "How to handle on creation and modification of a file within the watched directory."
  [file-path model-paths output-dir-path]
  (cond
    ;; If it's an EGX file, generate straight away.
    (egx? file-path)
    (try
      (generate file-path model-paths output-dir-path)
      (catch ExceptionInfo e
        (log/error "Exception found when trying to hot-reload" file-path)
        (.printStackTrace e)))

    ;; If it's an EGL file, find the corresponding EGX file and generate it.
    ;; If no EGX file was found, do nothing.
    (egl? file-path)
    (try
      (let [egx-file-path (replace-ext file-path "egx")]
        (if (fs/exists? egx-file-path)
          (try
            (generate egx-file-path model-paths output-dir-path)
            (catch ExceptionInfo e
              (log/error "Exception found when trying to hot-reload" file-path)
              (.printStackTrace e))))))

    ;; Nothing yet when EOL is created/modified. One thing we can do is to figure out
    ;; which modules depend on this EOL, then trigger a hot reload on the leaf modules
    ;; since doing so will trigger a down-cascade hot-reload on all relevant dependent
    ;; modules. But meh maybe later.
    (eol? file-path)
    (log/warn "No EOL support yet.")))

(defn watch
  [root-dir model-paths output-dir-path]
  (hawk/watch! [{:paths   [root-dir]
                 :filter  (fn [_ {:keys [file]}]
                            (and
                              (.isFile file)
                              (egx? file)
                              (egl? file)))
                 :handler (fn [ctx {:keys [file kind]}]
                            (case kind
                              :create (create-modify-handler file model-paths output-dir-path)
                              :modify (create-modify-handler file model-paths output-dir-path)
                              :delete (pprint (str "Delete file " file)))
                            ctx)}]))

(defn generate-all
  "Go through the provided template directory and generate everything.

  If watch? is true, return the watcher handler to be called to stop the watcher."
  [template-dir model-paths output-dir-path watch?]
  (let [egx-files (path->egx-files template-dir)]
    ;; Need to parallelise this somehow.
    (doall (map #(generate % model-paths output-dir-path) egx-files)))
  (if watch?
    (let [handler (watch template-dir model-paths output-dir-path)]
      (fn [] (hawk/stop! handler)))))
