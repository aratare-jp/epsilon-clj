(ns altio.edn-model
  (:import [org.eclipse.epsilon.eol.models CachedModel]
           [org.eclipse.epsilon.egl EgxModule EglFileGeneratingTemplateFactory]
           [java.io File]
           [org.eclipse.epsilon.emc.plainxml PlainXmlModel]))

(defn run []
  (let [module (new EgxModule (new EglFileGeneratingTemplateFactory))]
    (.parse module (-> (new File "resources/templates/main.egx") .getAbsoluteFile))
    (if (not (-> module .getParseProblems .isEmpty)) (throw (new RuntimeException "Parse problem found")))
    (let [model (new PlainXmlModel)]
      (.setFile model (new File "resources/templates/library.xml"))
      (.setName model "L")
      (.load model)
      (-> module .getContext .getModelRepository (.addModel model))
      (.execute module)
      (println "Done"))))

(run)