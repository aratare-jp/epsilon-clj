(ns epsilon.utility
  (:require [me.raynes.fs :as fs]
            [taoensso.timbre :as log])
  (:import [java.io File]
           [clojure.lang ExceptionInfo]))

(defmulti is-ext?
          "Check if a given path or file has the given extension."
          (fn [path _] (class path)))

(defmethod is-ext? String
  [path ext]
  (let [ext (if (= \. (first ext)) ext (str "." ext))]
    (= ext (fs/extension path))))

(defmethod is-ext? File
  [path ext]
  (is-ext? (.getAbsolutePath path) ext))

(defmethod is-ext? :default
  [_ _]
  (throw (ex-info "Unknown type. Only support String and File." {})))
;(defn is-ext?
;  "Check if a given path or file has the given extension."
;  [path ext]
;  (let [ext (if (= \. (first ext)) ext (str "." ext))]
;    (if (string? path)
;      (= ext (fs/extension path))
;      (= ext (fs/extension (.getAbsolutePath path))))))

(defn egx?
  "Check if a given path or file is an EGX file."
  [path]
  (is-ext? path ".egx"))

(defn egl?
  "Check if a given path or file is an EGL file."
  [path]
  (is-ext? path ".egl"))

(defn eol?
  "Check if a given path or file is an EOL file."
  [path]
  (is-ext? path ".eol"))

(defn evl?
  "Check if a given path or file is an EVL file."
  [path]
  (is-ext? path ".evl"))

(defn xml?
  "Check if a given path or file is an XML file."
  [path]
  (is-ext? path ".xml"))

(defn replace-ext
  "Replace the given file's extension with the given extension.

  If the extension is not prefixed with a dot, add one automatically.

  Returns the new path with no side-effect (no file created/modified/etc.)"
  [target ^String ext]
  (let [file-path   (if (string? target) target (.getAbsolutePath target))
        dot-index   (.lastIndexOf file-path ".")
        no-ext-file (subs file-path 0 dot-index)]
    (if (= \. (first ext))
      (str no-ext-file ext)
      (str no-ext-file "." ext))))

(defn handle-exception [f]
  (try
    (f)
    (catch Exception e
      (if (= ExceptionInfo (class e))
        (let [payload (:payload (.getData e))]
          (if (seq? payload)
            (doall (map #(log/error %) payload))
            (log/error payload)))
        (log/error e)))))