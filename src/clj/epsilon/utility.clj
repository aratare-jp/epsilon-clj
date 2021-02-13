(ns epsilon.utility
  (:require [me.raynes.fs :as fs]))

(defn is-ext?
  "Check if a given path or file has the given extension."
  [path ext]
  (if (fs/file? path)
    (= ext (fs/extension (.toPath path)))
    (= ext (fs/extension path))))

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
