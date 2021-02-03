(ns altio.generator
  (:require [puget.printer :refer [pprint]]
            [me.raynes.fs :as fs]
            [hawk.core :as hawk])
  (:import [org.eclipse.epsilon.egl EgxModule]
           [org.eclipse.epsilon.emc.plainxml PlainXmlModel]
           [org.eclipse.epsilon.eol.exceptions EolRuntimeException]
           [altio CustomEglFileGeneratingTemplateFactory]))

(defn generate
  "Generate an EGX file with the given XML model. Return the root directory, the EGX file and the EGX module."
  [{:keys [root file]} models output-dir]
  (let [template-factory (new CustomEglFileGeneratingTemplateFactory)]
    (.setOutputRoot template-factory (fs/file output-dir))
    (let [module (new EgxModule template-factory)]
      (.parse module (fs/file root file))
      (if (not (-> module .getParseProblems .isEmpty))
        (do
          (pprint (.getParseProblems module))
          (throw (new RuntimeException "Parse problem found"))))
      (doall
        (for [model models]
          (let [xml-model (new PlainXmlModel)]
            (.setFile xml-model (fs/file model))
            (.setName xml-model (fs/name model))
            (.load xml-model)
            (-> module .getContext .getModelRepository (.addModel xml-model)))))
      (try
        (.execute module)
        (catch EolRuntimeException e
          (pprint (.getMessage e))))
      {:root   root
       :file   file
       :module module})))

(defn watch
  [root-dir egx-files models output-dir]
  (hawk/watch! [{:paths   [root-dir]
                 :filter  (fn [ctx {:keys [file kind]}]
                            (and
                              (= :modify kind)
                              (.isFile file)
                              (= ".egx" (fs/extension (.toPath file)))))
                 :handler (fn [ctx {:keys [file kind]}]
                            (case kind
                              :create
                              (let [root (-> file .getParentFile .getAbsoluteFile)
                                    file (.getName file)]
                                (conj egx-files (generate {:root root :file file} models output-dir)))
                              :modify
                              (do
                                (let [root (-> file .getParentFile .getAbsoluteFile)
                                      file (.getName file)]
                                  (generate {:root root :file file} models output-dir)))
                              :delete (pprint (str "Delete file " file)))
                            ctx)}]))

(defn generate-all
  "Goes through the provided directory and generate everything."
  [{:keys [dir watch? models output-dir]}]
  (let [;; Walk the given dir to get all the files in form of a 2D list
        egx-files (fs/walk
                    (fn [root dirs files]
                      (-> files
                          ((partial filter #(= (fs/extension %) ".egx")))
                          ((partial mapv #(assoc {:root root} :file %)))))
                    dir)
        ;; Convert the 2D list into a 1D list for better consumption
        egx-files (reduce (fn [val new-val] (into val new-val)) [] egx-files)]
    (map #(generate % models output-dir) egx-files)
    (if watch?
      (watch dir egx-files models output-dir))))

(comment
  (generate "C:\\Users\\aratare\\projects\\altio\\resources\\templates" "main.egx" ["C:\\Users\\aratare\\projects\\altio\\resources\\templates\\library.xml"])
  (def watcher (generate-all {:dir        "resources/templates"
                              :models     ["C:\\Users\\aratare\\projects\\altio\\resources\\templates\\library.xml"]
                              :output-dir "resources/new-gen"
                              :watch?     true}))
  (hawk/stop! watcher))