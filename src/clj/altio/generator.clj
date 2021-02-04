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
  ([{:keys [root file]} models output-dir]
   (let [output-dir       (fs/file output-dir)
         file             (fs/file root file)
         template-factory (doto (new CustomEglFileGeneratingTemplateFactory)
                            (.setOutputRoot output-dir))
         module           (doto (new EgxModule template-factory)
                            (.parse file))]
     ;; Check if there is no parsing errors.
     (if (not (-> module .getParseProblems .isEmpty))
       (do
         (pprint (.getParseProblems module))
         (throw (new RuntimeException "Parse problem found"))))
     ;; Add all the models to the module.
     (map
       (fn [model]
         (let [model-file (fs/file model)
               model-name (fs/name model)
               model      (doto (new PlainXmlModel)
                            (.setFile model-file)
                            (.setName model-name)
                            (.load))]
           (-> module .getContext .getModelRepository (.addModel model))))
       models)
     ;; Execute the module.
     (try
       (.execute module)
       {:root   root
        :file   file
        :module module}
       (catch EolRuntimeException e
         (pprint (.getMessage e))
         {:exception e})))))

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
                          (->> (filter #(= (fs/extension %) ".egx")))
                          (->> (mapv #(assoc {:root root} :file %)))))
                    dir)
        ;; Convert the 2D list into a 1D list for better consumption
        egx-files (reduce (fn [val new-val] (into val new-val)) [] egx-files)]
    (map #(generate % models output-dir) egx-files)
    (if watch?
      (let [handler (watch dir egx-files models output-dir)]
        (fn [] (hawk/stop! handler))))))

(comment
  (generate "C:\\Users\\aratare\\projects\\altio\\resources\\templates" "main.egx" ["C:\\Users\\aratare\\projects\\altio\\resources\\templates\\library.xml"])
  (def watcher (generate-all {:dir        "resources/templates"
                              :models     ["C:\\Users\\aratare\\projects\\altio\\resources\\templates\\library.xml"]
                              :output-dir "resources/new-gen"
                              :watch?     true}))
  (hawk/stop! watcher))