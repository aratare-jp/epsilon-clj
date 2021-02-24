(ns epsilon.generator-test
  (:require [clojure.test :refer :all]
            [epsilon.generator :refer :all]
            [me.raynes.fs :as fs]
            [epsilon.utility :refer :all]
            [expectations.clojure.test :refer [defexpect expect more]])
  (:import [clojure.lang ExceptionInfo]
           [org.eclipse.epsilon.egl.exceptions EglRuntimeException]
           [org.eclipse.epsilon.eol.exceptions.models EolModelLoadingException]))


(defn once-fixture [f]
  (fs/delete-dir "test/resources/actual")
  (f)
  (fs/delete-dir "test/resources/actual"))

(use-fixtures :once once-fixture)

(defexpect generate-test
  (testing "Normal generation"
    (let [egx-file-path        "test/resources/templates/generate_test/book.html.egx"
          model-paths          ["test/resources/templates/generate_test/library.xml"]
          output-path          "test/resources/actual/generate_test"
          expected-output-path "test/resources/expected/generate_test"]
      (let [module (generate egx-file-path model-paths output-path)]
        (expect (complement nil?) module))
      (let [expected-files (-> expected-output-path fs/list-dir sort vec)
            actual-files   (-> output-path fs/list-dir sort vec)]
        (expect (count expected-files) (count actual-files))
        (expect (map #(fs/name %) expected-files) (map #(fs/name %) actual-files))
        (doall (for [[expected-file actual-file] (partition 2 (interleave expected-files actual-files))]
                 (expect (slurp expected-file) (slurp actual-file)))))))

  (testing "EGX parsing problems"
    (let [egx-file-path "test/resources/templates/generate_fail_test/book.html.egx"
          model-paths   ["test/resources/templates/generate_fail_test/library.xml"]
          output-path   "test/resources/actual/generate_fail_test"]
      (expect ExceptionInfo (generate egx-file-path model-paths output-path))))

  (testing "EGX executing problems"
    (let [egx-file-path "test/resources/templates/generate_fail_test/library.html.egx"
          model-paths   ["test/resources/templates/generate_fail_test/library.xml"]
          output-path   "test/resources/actual/generate_fail_test"]
      (expect EglRuntimeException (generate egx-file-path model-paths output-path))))

  (testing "Generate failure with invalid model"
    (let [egx-file-path "test/resources/templates/generate_fail_test/library.html.egx"
          model-paths   ["test/resources/templates/generate_fail_test/library/xml"]
          output-path   "test/resources/actual/generate_fail_test"]
      (expect EolModelLoadingException (generate egx-file-path model-paths output-path)))))

(defexpect generate-all-test
  (testing "Normal generate all"
    (let [template-dir         "test/resources/templates/generate_all_test"
          model-paths          ["test/resources/templates/generate_all_test/library.xml"]
          output-path          "test/resources/actual/generate_all_test"
          expected-output-path "test/resources/expected/generate_all_test"
          walker               (fn [root dirs files]
                                 {:dirs  dirs
                                  :files (map (fn [file]
                                                {:name    file
                                                 :content (slurp (fs/file root file))})
                                              files)})
          {:keys [modules watcher]} (generate-all template-dir model-paths output-path false)]
      (doall (for [{:keys [module problems? exception]} modules]
               (do
                 (expect (complement nil?) module)
                 (expect nil? problems?)
                 (expect nil? exception))))
      (expect nil? watcher)
      (let [expected (->> expected-output-path (fs/walk walker) vec)
            actual   (->> output-path (fs/walk walker) vec)]
        (expect (count expected) (count actual))
        (doall (for [[expected-dirs-files actual-dirs-files] (partition 2 (interleave expected actual))]
                 (do
                   (let [expected-dir   (-> (:dirs expected-dirs-files) vec sort)
                         actual-dir     (-> (:dirs actual-dirs-files) vec sort)
                         expected-files (->> (:files expected-dirs-files) vec (sort-by :name))
                         actual-files   (->> (:files actual-dirs-files) (sort-by :name))]
                     (expect expected-dir actual-dir)
                     (for [[expected-file actual-file] (partition 2 (interleave expected-files actual-files))]
                       (expect expected-file actual-file)))))))))

  (testing "Normal generate all in watch mode with EGX file change"
    (let [template-dir       "test/resources/templates/generate_all_test"
          model-paths        ["test/resources/templates/generate_all_test/library.xml"]
          output-path        "test/resources/actual/generate_all_test"
          watcher            (generate-all template-dir model-paths output-path true)
          egx-file           (fs/file template-dir "book.html.egx")
          before-egx-content (slurp egx-file)
          before-content     (map #(slurp (fs/file output-path %)) ["EMPBook.html" "EMFBook.html"])]
      (try
        (do (spit egx-file (str "\n\n\n\t\t\t" before-egx-content))
            ;; Sleep for a bit to wait for the watcher to do its work.
            (Thread/sleep 1000)
            (let [after-content (map #(slurp (fs/file output-path %)) ["EMPBook.html" "EMFBook.html"])]
              (expect before-content after-content)))
        (finally (do
                   ((:handler watcher))
                   (spit egx-file before-egx-content))))))

  (testing "Normal generate all in watch mode with EGL file change"
    (let [template-dir         "test/resources/templates/generate_all_test"
          model-paths          ["test/resources/templates/generate_all_test/library.xml"]
          output-path          "test/resources/actual/generate_all_test"
          expected-output-path "test/resources/expected/generate_all_watch_egl"
          watcher              (generate-all template-dir model-paths output-path true)
          egl-file             (fs/file template-dir "book.html.egl")
          before-egl-content   (slurp egl-file)
          expected-content     (map #(slurp (fs/file expected-output-path %))
                                    ["EMPBook.html" "EMFBook.html"])]
      (try
        (do (spit egl-file (str "<h1>Hello world!</h1>\n" before-egl-content))
            ;; Sleep for a bit to wait for the watcher to do its work.
            (Thread/sleep 1000)
            (let [after-content (map #(slurp (fs/file output-path %)) ["EMPBook.html" "EMFBook.html"])]
              (expect expected-content after-content)))
        (finally (do
                   ((:handler watcher))
                   (spit egl-file before-egl-content))))))

  (testing "Failed generate all with invalid models"
    (let [template-dir "test/resources/templates/generate_all_fail_parsing_test"
          model-paths  ["test/resources/templates/generate_all_fail_test/library/xml"]
          output-path  "test/resources/actual/generate_all_fail_test"]
      (expect EolModelLoadingException (generate-all template-dir model-paths output-path false))))

  (testing "Failed generate all with parsing problems"
    (let [template-dir "test/resources/templates/generate_all_fail_parsing_test"
          model-paths  ["test/resources/templates/generate_all_fail_parsing_test/library.xml"]
          output-path  "test/resources/actual/generate_all_fail_parsing_test"]
      (expect ExceptionInfo (generate-all template-dir model-paths output-path false))))

  (testing "Failed generate all with EGX executing problems"
    (let [template-dir "test/resources/templates/generate_all_fail_executing_test"
          model-paths  ["test/resources/templates/generate_all_fail_executing_test/library.xml"]
          output-path  "test/resources/actual/generate_all_fail_test"]
      (expect EglRuntimeException (generate-all template-dir model-paths output-path false)))))

(defexpect validate-test
  (testing "Normal validate"
    (let [evl-path    "test/resources/templates/validate_test/library.evl"
          model-paths ["test/resources/templates/validate_test/library.xml"]]
      (let [module (validate evl-path model-paths)]
        (expect (complement nil?) module))))

  (testing "Failed validate"
    (let [evl-path    "test/resources/templates/validate_fail_test/book.evl"
          model-paths ["test/resources/templates/validate_fail_test/library.xml"]]
      (expect ExceptionInfo (validate evl-path model-paths))))

  (testing "Failed validate with parsing problems"
    (let [evl-path    "test/resources/templates/validate_fail_test/library.evl"
          model-paths ["test/resources/templates/validate_fail_test/library.xml"]]
      (expect ExceptionInfo (validate evl-path model-paths)))))

(defexpect validate-all-test
  (testing "Normal validate all"
    (let [evl-path    "test/resources/templates/validate_all_test"
          model-paths ["test/resources/templates/validate_all_test/library.xml"]]
      (let [modules (validate-all evl-path model-paths false)]
        (expect (more (complement nil?) seq?) modules))))

  (testing "Failed validate all"
    (let [evl-path    "test/resources/templates/validate_all_fail_test"
          model-paths ["test/resources/templates/validate_all_fail_test/library.xml"]]
      (expect ExceptionInfo (validate-all evl-path model-paths false))))

  (testing "Failed validate all with parsing problems"
    (let [evl-path    "test/resources/templates/validate_all_fail_test"
          model-paths ["test/resources/templates/validate_all_fail_test/library.xml"]]
      (expect ExceptionInfo (validate-all evl-path model-paths false)))))
