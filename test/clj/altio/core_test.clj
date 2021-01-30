(ns altio.core-test
  (:require [clojure.test :refer :all]
    ;[altio.core :as core]
            [clojure.pprint :refer [pprint]])
  (:import [java.util Map]
           [org.stringtemplate.v4 STRawGroupDir]
           [com.altio KeywordAdaptor]))

(deftest example-test
  (let [^STRawGroupDir stg (new STRawGroupDir "templates")
        _                  (.registerModelAdaptor stg Map (new KeywordAdaptor *out*))
        st                 (.getInstanceOf stg "HelloWorld.java")
        entityAttrs        [{:type "String" :name "firstName"}
                            {:type "String" :name "lastName"}
                            {:type "int" :name "phoneNumber"}]
        prs                {"Insert properties here" "Foo Foo"
                            "Insert methods here"    "Bar Bar"}]
    (.add st "entityAttrs" entityAttrs)
    (.add st "prs" prs)
    (print (.render st))))
