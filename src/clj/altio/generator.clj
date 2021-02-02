(ns altio.generator
  (:require [puget.printer :refer [pprint]]
            [me.raynes.fs :as fs]
            [clojure.string :as string])
  (:import [org.eclipse.epsilon.egl EgxModule]
           [org.eclipse.epsilon.emc.plainxml PlainXmlModel]
           [org.eclipse.epsilon.eol.exceptions EolRuntimeException]
           [altio CustomEglFileGeneratingTemplateFactory]))

(defn generate
  "Generate an EGX file with the given XML model"
  [root file models output-dir]
  (pprint (str "Root: " root))
  (pprint (str "File: " file))
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
      (println "Done"))))

(defn generate-all
  "Goes through the provided directory and generate everything."
  [{:keys [dir watch models output-dir]}]
  (fs/walk
    (fn [root dirs files]
      (-> files
          ((partial filter #(= (fs/extension %) ".egx")))
          ((partial map #(generate root % models output-dir)))))
    dir))

(comment
  (generate "C:\\Users\\aratare\\projects\\altio\\resources\\templates" "main.egx" ["C:\\Users\\aratare\\projects\\altio\\resources\\templates\\library.xml"])
  (generate-all {:dir        "resources/templates"
                 :models     ["C:\\Users\\aratare\\projects\\altio\\resources\\templates\\library.xml"]
                 :output-dir "resources/new-gen"}))