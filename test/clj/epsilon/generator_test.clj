(ns epsilon.generator-test
  (:require [clojure.test :refer :all]
            [epsilon.generator :refer :all]
            [me.raynes.fs :as fs]
            [puget.printer :refer [pprint]]
            [taoensso.timbre :as log]
            [epsilon.utility :refer :all]
            [spy.core :as sc]
            [expectations.clojure.test :refer [defexpect expect] :as ex]))

(def refresh-content
  "Declare which file need to be refreshed on every test"
  ["test/resources/templates/generate_all_test/book.html.egl"
   "test/resources/templates/generate_all_test/book.html.egx"])

(defn each-fixture [f]
  ;; Used to cache a template content so we can modify it freely within the test.
  (let [original (map (fn [f] {:file f :content (slurp f)}) refresh-content)]
    (pprint original)
    (f)
    (doall (for [{:keys [file content]} original] (spit file content)))))

(defn once-fixture [f]
  (fs/delete-dir "test/resources/actual")
  (f)
  (fs/delete-dir "test/resources/actual"))

(use-fixtures :each each-fixture)
(use-fixtures :once once-fixture)

(defexpect generate-test
  (testing "Normal generation"
    (let [egx-file-path            "test/resources/templates/generate_test/book.html.egx"
          model-paths              ["test/resources/templates/generate_test/library.xml"]
          output-dir-path          "test/resources/actual/generate_test"
          expected-output-dir-path "test/resources/expected/generate_test"]
      (let [result (generate egx-file-path model-paths output-dir-path)]
        (expect nil? (:problems? result))
        (expect nil? (:exception result))
        (expect (complement nil?) (:module result)))
      (let [expected-files (-> expected-output-dir-path fs/list-dir sort vec)
            actual-files   (-> output-dir-path fs/list-dir sort vec)]
        (expect (count expected-files) (count actual-files))
        (expect (map #(fs/name %) expected-files) (map #(fs/name %) actual-files))
        (doall (for [[expected-file actual-file] (partition 2 (interleave expected-files actual-files))]
                 (expect (slurp expected-file) (slurp actual-file)))))))

  (testing "EGX parsing problems"
    (let [egx-file-path   "test/resources/templates/generate_fail_test/book.html.egx"
          model-paths     ["test/resources/templates/generate_fail_test/library.xml"]
          output-dir-path "test/resources/actual/generate_fail_test"]
      (let [{:keys [module problems? exception]} (generate egx-file-path model-paths output-dir-path)]
        (expect nil? module)
        (expect (complement nil?) problems?)
        (expect nil? exception))))

  (testing "EGX executing problems"
    (let [egx-file-path   "test/resources/templates/generate_fail_test/library.html.egx"
          model-paths     ["test/resources/templates/generate_fail_test/library.xml"]
          output-dir-path "test/resources/actual/generate_fail_test"]
      (let [{:keys [module problems? exception]} (generate egx-file-path model-paths output-dir-path)]
        (expect nil? module)
        (expect nil? problems?)
        (expect (complement nil?) exception))))

  (testing "Generate failure with invalid model"
    (let [egx-file-path   "test/resources/templates/generate_fail_test/library.html.egx"
          model-paths     ["test/resources/templates/generate_fail_test/library/xml"]
          output-dir-path "test/resources/actual/generate_fail_test"]
      (let [{:keys [module problems? exception]} (generate egx-file-path model-paths output-dir-path)]
        (expect nil? module)
        (expect nil? problems?)
        (expect (complement nil?) exception)))))

(defexpect generate-all-test
  (testing "Normal generate all"
    (let [template-dir             "test/resources/templates/generate_all_test"
          model-paths              ["test/resources/templates/generate_all_test/library.xml"]
          output-dir-path          "test/resources/actual/generate_all_test"
          expected-output-dir-path "test/resources/expected/generate_all_test"
          walker                   (fn [root dirs files]
                                     {:dirs  dirs
                                      :files (map (fn [file]
                                                    {:name    file
                                                     :content (slurp (fs/file root file))})
                                                  files)})
          {:keys [modules watcher]} (generate-all template-dir model-paths output-dir-path false)]
      (doall (for [{:keys [module problems? exception]} modules]
               (do
                 (expect (complement nil?) module)
                 (expect nil? problems?)
                 (expect nil? exception))))
      (expect nil? watcher)
      (let [expected (->> expected-output-dir-path (fs/walk walker) vec)
            actual   (->> output-dir-path (fs/walk walker) vec)]
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
          output-dir-path    "test/resources/actual/generate_all_test"
          watcher            (generate-all template-dir model-paths output-dir-path true)
          egx-file           (fs/file template-dir "book.html.egx")
          before-egx-content (slurp egx-file)
          before-content     (map #(slurp (fs/file output-dir-path %)) ["EMPBook.html" "EMFBook.html"])]
      (try
        (do (spit egx-file (str "\n\n\n\t\t\t" before-egx-content))
            ;; Sleep for a bit to wait for the watcher to do its work.
            (Thread/sleep 1000)
            (let [after-content (map #(slurp (fs/file output-dir-path %)) ["EMPBook.html" "EMFBook.html"])]
              (expect before-content after-content)))
        (finally ((-> watcher :watcher :handler))))))

  (testing "Normal generate all in watch mode with EGL file change"
    (let [template-dir             "test/resources/templates/generate_all_test"
          model-paths              ["test/resources/templates/generate_all_test/library.xml"]
          output-dir-path          "test/resources/actual/generate_all_test"
          expected-output-dir-path "test/resources/expected/generate_all_watch_egl"
          watcher                  (generate-all template-dir model-paths output-dir-path true)
          egl-file                 (fs/file template-dir "book.html.egl")
          before-egl-content       (slurp egl-file)
          expected-content         (map #(slurp (fs/file expected-output-dir-path %))
                                        ["EMPBook.html" "EMFBook.html"])]
      (try
        (do (spit egl-file (str "<h1>Hello world!</h1>\n" before-egl-content))
            ;; Sleep for a bit to wait for the watcher to do its work.
            (Thread/sleep 1000)
            (let [after-content (map #(slurp (fs/file output-dir-path %)) ["EMPBook.html" "EMFBook.html"])]
              (expect expected-content after-content)))
        (finally ((-> watcher :watcher :handler)))))))
