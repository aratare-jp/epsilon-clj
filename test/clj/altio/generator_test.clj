(ns altio.generator-test
  (:require [clojure.test :refer :all]
            [altio.generator :refer :all]
            [me.raynes.fs :as fs]
            [puget.printer :refer [pprint]]))

(deftest test
  (generate "test/resources/templates/main.egx"
            ["test/resources/templates/library.xml"]
            "test/resources/gen"))