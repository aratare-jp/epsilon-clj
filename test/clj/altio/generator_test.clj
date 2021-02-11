(ns altio.generator-test
  (:require [clojure.test :refer :all]
            [altio.generator :refer :all]
            [me.raynes.fs :as fs]
            [puget.printer :refer [pprint]]
            [taoensso.timbre :as log]))

(def template-file "test/resources/templates/book.html.egx")
(def template-dir "test/resources/templates")
(def model-paths ["test/resources/templates/library.xml"])
(def actual-output-dir-path "test/resources/actual")

(def expected-generate-test-dir-path "test/resources/expected/generate_test")
(def actual-generate-test-dir-path "test/resources/actual/generate_test")

(def expected-generate-all-test-dir-path "test/resources/expected/generate_all_test")
(def actual-generate-all-test-dir-path "test/resources/actual/generate_all_test")

(defn preparing-output-dir [f]
  (if (fs/exists? actual-output-dir-path)
    (fs/delete-dir actual-output-dir-path))
  (f)
  (if (fs/exists? actual-output-dir-path)
    (fs/delete-dir actual-output-dir-path)))

(use-fixtures :each preparing-output-dir)

(deftest generate-test
  (generate template-file model-paths actual-generate-test-dir-path)
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
        expected-gen-files (->> expected-generate-all-test-dir-path (fs/walk walker))
        actual-gen-files   (->> actual-generate-all-test-dir-path (fs/walk walker))]
    (doall (for [i-dir (range (count expected-gen-files))]
             (let [expected-gen-dirs (-> expected-gen-files (nth i-dir) :dirs)
                   actual-gen-dirs   (-> actual-gen-files (nth i-dir) :dirs)]
               (is (= expected-gen-dirs actual-gen-dirs))
               (doall (for [i-file (range (count (nth expected-gen-files i-dir)))]
                        (let [expected-gen-file (-> expected-gen-files (nth i-dir) :files (->> (sort-by :name)) (nth i-file))
                              actual-gen-file   (-> actual-gen-files (nth i-dir) :files (->> (sort-by :name)) (nth i-file))]
                          (is (= (:name expected-gen-file) (:name actual-gen-file)))
                          (is (= (:content expected-gen-file) (:content actual-gen-file)))))))))))