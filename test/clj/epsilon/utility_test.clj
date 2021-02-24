(ns epsilon.utility-test
  (:require [clojure.test :refer :all]
            [expectations.clojure.test :refer :all]
            [epsilon.utility :refer :all]
            [me.raynes.fs :as fs]))

(defexpect is-ext?-test
  (testing "String - Name only"
    (expect (is-ext? "test.egl" ".egl"))
    (expect (not (is-ext? "test.egx" ".egl")))
    (expect (is-ext? "test.egl" "egl"))
    (expect (not (is-ext? "test.egx" "egl"))))

  (testing "String - Name only"
    (expect (is-ext? "home/foo/bar/text.egl" ".egl"))
    (expect (not (is-ext? "home/foo/bar/text.egx" ".egl")))
    (expect (is-ext? "home/foo/bar/text.egl" "egl"))
    (expect (not (is-ext? "home/foo/bar/text.egx" "egl"))))

  (testing "File"
    (let [file (fs/file "test/resources/templates/generate_all/library.html.egl")]
      (expect (is-ext? file "egl"))
      (expect (not (is-ext? file "egx")))
      (expect (is-ext? file ".egl"))
      (expect (not (is-ext? file ".egx"))))))

(defexpect replace-ext-test
  (testing "String - Name only"
    (expect "test.egx" (replace-ext "test.egl" "egx"))
    (expect "test.egx" (replace-ext "test.egl" ".egx")))

  (testing "String - Path"
    (expect "home/foo/bar/text.egx" (replace-ext "home/foo/bar/text.egl" "egx"))
    (expect "home/foo/bar/text.egx" (replace-ext "home/foo/bar/text.egl" ".egx")))

  (testing "File"
    (let [file     (fs/file "test/resources/templates/generate_all/library.html.egl")
          expected (->> "test/resources/templates/generate_all/library.html.egx"
                        (fs/file (System/getProperty "user.dir"))
                        (.getAbsolutePath))]
      (expect expected (replace-ext file "egx"))
      (expect expected (replace-ext file ".egx")))))