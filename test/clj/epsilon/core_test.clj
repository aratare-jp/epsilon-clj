(ns epsilon.core-test
  (:require [clojure.test :refer :all]
            [epsilon.core :refer [-main add-shutdown-hook exit actions-map]]
            [expectations.clojure.test :refer :all]
            [spy.core :as s]
            [spy.core :as spy]
            [taoensso.timbre :as log]))

(defexpect core-test
  (testing "Normal generate"
    (let [template-dir "test/resources/templates/generate_all_test"
          model-path   "test/resources/templates/generate_all_test/library.xml"
          output-path  "test/resources/actual/main_test"
          watch?       false]
      (with-redefs [add-shutdown-hook (s/spy (fn [hs] (expect (seq? hs))))
                    exit              (s/spy (fn [_ msg] (log/error msg)))
                    actions-map       {:generate (s/spy (fn [opts]
                                                          (expect template-dir (:template-dir opts))
                                                          (expect [model-path] (:model-paths opts))
                                                          (expect output-path (:output-path opts))
                                                          (expect watch? (:watch? opts))))
                                       :validate (s/spy (fn [opts]
                                                          (expect template-dir (:template-dir opts))
                                                          (expect [model-path] (:model-paths opts))
                                                          (expect output-path (:output-path opts))
                                                          (expect watch? (:watch? opts))))}]
        (let [_ (-main "-d" template-dir "-m" model-path "-o" output-path "generate")]
          (expect spy/called? (get actions-map :generate))
          (expect spy/not-called? (get actions-map :validate))
          (expect spy/not-called? exit)
          (expect spy/not-called? add-shutdown-hook)))))

  (testing "Normal validate"
    (let [template-dir "test/resources/templates/generate_all_test"
          model-path   "test/resources/templates/generate_all_test/library.xml"
          output-path  "test/resources/actual/main_test"
          watch?       false]
      (with-redefs [add-shutdown-hook (s/spy (fn [hs] (expect (seq? hs))))
                    exit              (s/spy (fn [_ msg] (log/error msg)))
                    actions-map       {:generate (s/spy (fn [opts]
                                                          (expect template-dir (:template-dir opts))
                                                          (expect [model-path] (:model-paths opts))
                                                          (expect output-path (:output-path opts))
                                                          (expect watch? (:watch? opts))))
                                       :validate (s/spy (fn [opts]
                                                          (expect template-dir (:template-dir opts))
                                                          (expect [model-path] (:model-paths opts))
                                                          (expect output-path (:output-path opts))
                                                          (expect watch? (:watch? opts))))}]
        (let [_ (-main "-d" template-dir "-m" model-path "-o" output-path "validate")]
          (expect spy/called? (get actions-map :validate))
          (expect spy/not-called? (get actions-map :generate))
          (expect spy/not-called? exit)
          (expect spy/not-called? add-shutdown-hook))))))
