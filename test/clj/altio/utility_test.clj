(ns altio.utility-test
  (:require [clojure.test :refer :all]
            [altio.utility :refer :all]))

(deftest replace-ext-test
  (is (= "test.egx" (replace-ext "text.egl" "egx")))
  (is (= "test.egx" (replace-ext "text.egl" ".egx")))
  (is (= "home/foo/bar/text.egx" (replace-ext "home/foo/bar/text.egl" "egx")))
  (is (= "home/foo/bar/text.egx" (replace-ext "home/foo/bar/text.egl" "egx"))))