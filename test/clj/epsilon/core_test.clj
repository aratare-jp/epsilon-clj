(ns epsilon.core-test
  (:require [clojure.test :refer :all]
            [epsilon.core :refer :all]
            [expectations.clojure.test :refer :all]
            [spy.core :as s]
            [spy.core :as spy]
            [taoensso.timbre :as log]))

(defexpect cli-options-test
  (testing "Normal CLI options - generate"
    (let [template-dir "test/resources/templates/generate_all_test"
          model-path   "test/resources/templates/generate_all_test/library.xml"
          output-path  "test/resources/actual/main_test"
          args         ["-d" template-dir "-m" model-path "-o" output-path "generate"]
          {:keys [action options exit-message ok?]} (validate-args args)]
      (expect nil? exit-message)
      (expect nil? ok?)
      (expect :generate action)
      (expect {:template-dir template-dir
               :model-paths  [model-path]
               :output-path  output-path
               :min-level    0
               :watch?       false}
              options)))

  (testing "Normal CLI options - generate + no model"
    (let [template-dir "test/resources/templates/generate_all_test"
          output-path  "test/resources/actual/main_test"
          args         ["-d" template-dir "-o" output-path "generate"]
          {:keys [action options exit-message ok?]} (validate-args args)]
      (expect nil? exit-message)
      (expect nil? ok?)
      (expect :generate action)
      (expect {:template-dir template-dir
               :model-paths  []
               :output-path  output-path
               :min-level    0
               :watch?       false}
              options)))

  (testing "Normal CLI options - generate + watch"
    (let [template-dir "test/resources/templates/generate_all_test"
          model-path   "test/resources/templates/generate_all_test/library.xml"
          output-path  "test/resources/actual/main_test"
          args         ["-d" template-dir "-m" model-path "-o" output-path "-w" "generate"]
          {:keys [action options exit-message ok?]} (validate-args args)]
      (expect nil? exit-message)
      (expect nil? ok?)
      (expect :generate action)
      (expect {:template-dir template-dir
               :model-paths  [model-path]
               :output-path  output-path
               :min-level    0
               :watch?       true}
              options)))

  (testing "Normal CLI options - validate"
    (let [template-dir "test/resources/templates/generate_all_test"
          model-path   "test/resources/templates/generate_all_test/library.xml"
          output-path  "test/resources/actual/main_test"
          args         ["-d" template-dir "-m" model-path "-o" output-path "validate"]
          {:keys [action options exit-message ok?]} (validate-args args)]
      (expect nil? exit-message)
      (expect nil? ok?)
      (expect :validate action)
      (expect {:template-dir template-dir
               :model-paths  [model-path]
               :output-path  output-path
               :min-level    0
               :watch?       false}
              options)))

  (testing "Normal CLI options - validate + watch"
    (let [template-dir "test/resources/templates/generate_all_test"
          model-path   "test/resources/templates/generate_all_test/library.xml"
          output-path  "test/resources/actual/main_test"
          args         ["-d" template-dir "-m" model-path "-o" output-path "-w" "validate"]
          {:keys [action options exit-message ok?]} (validate-args args)]
      (expect nil? exit-message)
      (expect nil? ok?)
      (expect :validate action)
      (expect {:template-dir template-dir
               :model-paths  [model-path]
               :output-path  output-path
               :min-level    0
               :watch?       true}
              options)))

  (testing "Normal CLI options - verbosity"
    (let [template-dir "test/resources/templates/generate_all_test"
          model-path   "test/resources/templates/generate_all_test/library.xml"
          output-path  "test/resources/actual/main_test"
          args         ["-d" template-dir "-m" model-path "-o" output-path "-v" "generate"]
          {:keys [action options exit-message ok?]} (validate-args args)]
      (expect nil? exit-message)
      (expect nil? ok?)
      (expect :generate action)
      (expect {:template-dir template-dir
               :model-paths  [model-path]
               :output-path  output-path
               :min-level    1
               :watch?       false}
              options)))

  (testing "Normal CLI options - help"
    (let [args ["-h"]
          {:keys [action options exit-message ok?]} (validate-args args)]
      (expect (complement nil?) exit-message)
      (expect true ok?)
      (expect nil? action)
      (expect nil? options)))

  (testing "Failed CLI options - non-existing template directory"
    (let [template-dir "test/resources/templates/not_real"
          model-path   "test/resources/templates/generate_all_test/library.xml"
          output-path  "test/resources/actual/main_test"
          args         ["-d" template-dir "-m" model-path "-o" output-path "generate"]
          {:keys [action options exit-message ok?]} (validate-args args)]
      (expect (complement nil?) exit-message)
      (expect nil? ok?)
      (expect nil? action)
      (expect nil? options)))

  (testing "Failed CLI options - non-directory template directory"
    (let [template-dir "test/resources/templates/generate_all_test/library.xml"
          model-path   "test/resources/templates/generate_all_test/library.xml"
          output-path  "test/resources/actual/main_test"
          args         ["-d" template-dir "-m" model-path "-o" output-path "generate"]
          {:keys [action options exit-message ok?]} (validate-args args)]
      (expect (complement nil?) exit-message)
      (expect nil? ok?)
      (expect nil? action)
      (expect nil? options)))

  (testing "Failed CLI options - non-existing model path"
    (let [template-dir "test/resources/templates/generate_all_test"
          model-path   "test/resources/templates/not_real/library.xml"
          output-path  "test/resources/actual/main_test"
          args         ["-d" template-dir "-m" model-path "-o" output-path "generate"]
          {:keys [action options exit-message ok?]} (validate-args args)]
      (expect (complement nil?) exit-message)
      (expect nil? ok?)
      (expect nil? action)
      (expect nil? options)))

  (testing "Failed CLI options - non-directory model path"
    (let [template-dir "test/resources/templates/generate_all_test"
          model-path   "test/resources/templates/generate_all_test"
          output-path  "test/resources/actual/main_test"
          args         ["-d" template-dir "-m" model-path "-o" output-path "generate"]
          {:keys [action options exit-message ok?]} (validate-args args)]
      (expect (complement nil?) exit-message)
      (expect nil? ok?)
      (expect nil? action)
      (expect nil? options)))

  (testing "Failed CLI options - verbosity"
    (let [template-dir "test/resources/templates/generate_all_test"
          model-path   "test/resources/templates/generate_all_test/library.xml"
          output-path  "test/resources/actual/main_test"
          args         ["-d" template-dir "-m" model-path "-o" output-path "-vvv" "generate"]
          {:keys [action options exit-message ok?]} (validate-args args)]
      (expect (complement nil?) exit-message)
      (expect nil? ok?)
      (expect nil? action)
      (expect nil? options))))

(defexpect miscellaneous-test
  (testing "Normal filtered log middleware - Info"
    (let [msg           ["Hello" "Bob!"]
          appender-data {:level :info :vargs msg}]
      (expect nil? (filtered-log-middleware appender-data))))

  (testing "Normal filtered log middleware - Error"
    (let [msg           ["Hello" "Bob!"]
          appender-data {:level :error :vargs msg}]
      (expect nil? (filtered-log-middleware appender-data))))

  (testing "Normal filtered log middleware - Debug"
    (let [msg           ["Hello" "Bob!"]
          appender-data {:level :debug :vargs msg}]
      (expect appender-data (filtered-log-middleware appender-data)))))

(defexpect ^:eftest/synchronized config-log-default-test
  (testing "Normal config log - Default"
    (let [opts     {:min-level 0}
          expected {:min-level :info :middleware [filtered-log-middleware]}]
      (with-redefs [log/merge-config! (spy/spy (fn [m] (expect expected m)))]
        (config-log opts)
        (spy/called-once? log/merge-config!)))))

(defexpect ^:eftest/synchronized config-log-debug-test
  (testing "Normal config log - Debug"
    (let [opts     {:min-level 1}
          expected {:min-level :debug :middleware []}]
      (with-redefs [log/merge-config! (spy/spy (fn [m] (expect expected m)))]
        (config-log opts)
        (spy/called-once? log/merge-config!)))))

(defexpect ^:eftest/synchronized config-log-trace-test
  (testing "Normal config log - Trace"
    (let [opts     {:min-level 2}
          expected {:min-level :trace :middleware []}]
      (with-redefs [log/merge-config! (spy/spy (fn [m] (expect expected m)))]
        (config-log opts)
        (spy/called-once? log/merge-config!)))))

(defexpect core-test
  (testing "Normal generate"
    (let [template-dir "test/resources/templates/generate_all_test"
          model-path   "test/resources/templates/generate_all_test/library.xml"
          output-path  "test/resources/actual/main_test"
          watch?       false
          expected     {:template-dir template-dir
                        :model-paths  [model-path]
                        :output-path  output-path
                        :watch?       watch?
                        :min-level    0}]
      (with-redefs [add-shutdown-hook (s/spy (fn [hs] (expect (seq? hs))))
                    exit              (s/spy (fn [_ _] (expect false)))
                    config-log        (fn [_])
                    ;; We don't want to shut down the agent pool since other tests depend on this
                    shutdown-agents   (fn [])
                    actions-map       {:generate (s/spy (fn [opts] (expect expected opts)))
                                       :validate (s/spy (fn [opts] (expect expected opts)))}]
        (-main "-d" template-dir "-m" model-path "-o" output-path "generate")
        (expect spy/called? (get actions-map :generate))
        (expect spy/not-called? (get actions-map :validate))
        (expect spy/not-called? exit)
        (expect spy/not-called? add-shutdown-hook))))

  (testing "Failed generate - wrong arguments"
    (let [template-dir "test/resources/templates/non_exist"
          model-path   "test/resources/templates/generate_all_test/library.xml"
          output-path  "test/resources/actual/main_test"]
      (with-redefs [exit (s/spy (fn [code msg]
                                  (expect 1 code)
                                  (expect (complement nil?) msg)))]
        (-main "-d" template-dir "-m" model-path "-o" output-path "generate")
        (expect spy/called-once? exit))))

  (testing "Failed generate - invalid arguments"
    (with-redefs [exit (s/spy (fn [code msg]
                                (expect 1 code)
                                (expect (complement nil?) msg)))]
      (-main "--foo" "generate")
      (expect spy/called-once? exit)))

  (testing "Normal generate - help"
    (with-redefs [exit (s/spy (fn [code msg]
                                (expect 0 code)
                                (expect (complement nil?) msg)))]
      (-main "-h")
      (expect spy/called-once? exit)))

  (testing "Normal validate"
    (let [template-dir "test/resources/templates/generate_all_test"
          model-path   "test/resources/templates/generate_all_test/library.xml"
          output-path  "test/resources/actual/main_test"
          watch?       false
          expected     {:template-dir template-dir
                        :model-paths  [model-path]
                        :output-path  output-path
                        :watch?       watch?
                        :min-level    0}]
      (with-redefs [add-shutdown-hook (s/spy (fn [hs] (expect (seq? hs))))
                    exit              (s/spy (fn [_ msg] (log/error msg)))
                    config-log        (fn [_])
                    ;; We don't want to shut down the agent pool since other tests depend on this
                    shutdown-agents   (fn [])
                    actions-map       {:generate (s/spy (fn [opts] (expect expected opts)))
                                       :validate (s/spy (fn [opts] (expect expected opts)))}]
        (-main "-d" template-dir "-m" model-path "-o" output-path "validate")
        (expect spy/called? (get actions-map :validate))
        (expect spy/not-called? (get actions-map :generate))
        (expect spy/not-called? exit)
        (expect spy/not-called? add-shutdown-hook)))))
