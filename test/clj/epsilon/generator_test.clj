(ns epsilon.generator-test
  (:require [clojure.test :refer :all]
            [epsilon.generator :refer :all]
            [me.raynes.fs :as fs]
            [puget.printer :refer [pprint]]
            [taoensso.timbre :as log]
            [epsilon.utility :refer :all]))

(def template-egx-file "test/resources/templates/book.html.egx")
(def template-egl-file "test/resources/templates/book.html.egl")
(def template-dir "test/resources/templates")
(def model-paths ["test/resources/templates/library.xml"])

;; Actual and expected generated directories for comparison
(def actual-output-dir-path "test/resources/actual")
(def expected-generate-test-dir-path "test/resources/expected/generate_test")
(def actual-generate-test-dir-path "test/resources/actual/generate_test")
(def expected-generate-all-test-dir-path "test/resources/expected/generate_all_test")
(def actual-generate-all-test-dir-path "test/resources/actual/generate_all_test")
(def expected-watch-test-dir-path "test/resources/expected/watch_test")
(def actual-watch-egx-test-dir-path "test/resources/actual/watch_egx_test")
(def actual-watch-egl-test-dir-path "test/resources/actual/watch_egl_test")

(defn each-fixture [f]
  ;; Used to cache a template content so we can modify it freely within the test.
  (let [original-egx-content (slurp template-egx-file)
        original-egl-content (slurp template-egl-file)]
    (f)
    (spit template-egx-file original-egx-content)
    (spit template-egl-file original-egl-content)))

(defn once-fixture [f]
  (fs/delete-dir actual-output-dir-path)
  (f)
  (fs/delete-dir actual-output-dir-path))

(use-fixtures :each each-fixture)
(use-fixtures :once once-fixture)

(deftest generate-test
  (generate template-egx-file model-paths actual-generate-test-dir-path)
  (let [expected-gen-files (-> expected-generate-test-dir-path fs/list-dir sort vec)
        actual-gen-files   (-> actual-generate-test-dir-path fs/list-dir sort vec)]
    (is (= (count expected-gen-files) (count actual-gen-files)))
    (is (= (map #(fs/name %) expected-gen-files)
           (map #(fs/name %) actual-gen-files)))
    (doall (for [i (range (count expected-gen-files))]
             (let [expected-gen-file-content (slurp (nth expected-gen-files i))
                   actual-gen-file-content   (slurp (nth actual-gen-files i))]
               (is (= expected-gen-file-content actual-gen-file-content)))))))

(deftest generate-all-test
  (generate-all template-dir model-paths actual-generate-all-test-dir-path false)
  (let [walker             (fn [root dirs files]
                             {:dirs  dirs
                              :files (map (fn [file]
                                            {:name    file
                                             :content (slurp (fs/file root file))})
                                          files)})
        expected-gen-files (->> expected-generate-all-test-dir-path (fs/walk walker) vec)
        actual-gen-files   (->> actual-generate-all-test-dir-path (fs/walk walker) vec)]
    (is (= (count expected-gen-files) (count actual-gen-files)))
    (doall (for [i-dir (range (count expected-gen-files))]
             (let [expected-gen-dirs (-> expected-gen-files (nth i-dir) :dirs)
                   actual-gen-dirs   (-> actual-gen-files (nth i-dir) :dirs)]
               (is (= expected-gen-dirs actual-gen-dirs))
               (doall (for [i-file (range (count (nth expected-gen-files i-dir)))]
                        (let [expected-gen-file (-> expected-gen-files (nth i-dir) :files (->> (sort-by :name)) (nth i-file))
                              actual-gen-file   (-> actual-gen-files (nth i-dir) :files (->> (sort-by :name)) (nth i-file))]
                          (is (= (:name expected-gen-file) (:name actual-gen-file)))
                          (is (= (:content expected-gen-file) (:content actual-gen-file)))))))))))

(deftest watch-egx-test
  (let [expected-generated-emp-book-file-content (slurp (fs/file expected-watch-test-dir-path "EMPBook.html"))
        expected-generated-emf-book-file-content (slurp (fs/file expected-watch-test-dir-path "EMFBook.html"))
        watcher-handler                          (generate-all template-dir model-paths actual-watch-egx-test-dir-path true)
        template-content                         (slurp template-egx-file)]
    (try
      (do (spit template-egx-file (str "\n\n\n" template-content))
          ;; Sleep for a bit to wait for the watcher to do its work.
          (Thread/sleep 1000)
          (let [actual-generated-emp-book-file-content (slurp (fs/file actual-watch-egx-test-dir-path "EMPBook.html"))
                actual-generated-emf-book-file-content (slurp (fs/file actual-watch-egx-test-dir-path "EMFBook.html"))]
            (is (= expected-generated-emf-book-file-content actual-generated-emf-book-file-content))
            (is (= expected-generated-emp-book-file-content actual-generated-emp-book-file-content))))
      (finally (watcher-handler)))))

(deftest watch-egl-test
  (let [expected-generated-emp-book-file-content (slurp (fs/file expected-watch-test-dir-path "EMPBook.html"))
        expected-generated-emf-book-file-content (slurp (fs/file expected-watch-test-dir-path "EMFBook.html"))
        watcher-handler                          (generate-all template-dir model-paths actual-watch-egl-test-dir-path true)
        template-content                         (slurp template-egl-file)]
    (try
      (do (spit template-egl-file (str "<h1>Hello world!</h1>\n" template-content))
          ;; Sleep for a bit to wait for the watcher to do its work.
          (Thread/sleep 1000)
          (let [actual-generated-emp-book-file-content (slurp (fs/file actual-watch-egl-test-dir-path "EMPBook.html"))
                actual-generated-emf-book-file-content (slurp (fs/file actual-watch-egl-test-dir-path "EMFBook.html"))]
            (is (= expected-generated-emf-book-file-content actual-generated-emf-book-file-content))
            (is (= expected-generated-emp-book-file-content actual-generated-emp-book-file-content))))
      (finally (watcher-handler)))))