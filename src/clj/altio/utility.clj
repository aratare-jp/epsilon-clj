(ns altio.utility
  (:require [me.raynes.fs :as fs]
            [taoensso.timbre :as log]))

(defn egx?
  "Check if a given path or file is an EGX file."
  [path]
  (if (fs/file? path)
    (= ".egx" (fs/extension (.toPath path)))
    (= ".egx" (fs/extension path))))

(defn egl?
  "Check if a given path or file is an EGL file."
  [path]
  (if (fs/file? path)
    (= ".egl" (fs/extension (.toPath path)))
    (= ".egl" (fs/extension path))))

(defn eol?
  "Check if a given path or file is an EOL file."
  [path]
  (if (fs/file? path)
    (= ".eol" (fs/extension (.toPath path)))
    (= ".eol" (fs/extension path))))

(defn replace-ext
  "Replace the given file's extension with the given extension.

  If the extension is not prefixed with a dot, add one automatically.

  Returns the new path with no side-effect (no file created/modified/etc.)"
  [^String file-path ^String ext]
  (let [dot-index   (.lastIndexOf file-path ".")
        no-ext-file (subs file-path 0 dot-index)]
    (if (= \. (first ext))
      (str no-ext-file ext)
      (str no-ext-file "." ext))))

(comment
  (= "test.egx" (replace-ext "test.egl" "egx")))