(ns altio.core
  (:require [clj-antlr.core :as antlr]))

(defn walk
  "Walk a tree list representing the parsed code from Altio template."
  ([node]
   (.toString (walk node (new StringBuilder))))
  ([node sb]
   (if (seq? node)
     (do
       (doall (for [child (drop 1 node)] (walk child sb)))
       sb)
     (.append sb node))))
