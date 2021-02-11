(ns altio.core
  (:gen-class)
  (:require [puget.printer :refer [pprint]]
            [clojure.tools.cli :as cli]
            [clojure.string :as string]
            [altio.generator :refer [generate-all]]
            [me.raynes.fs :as fs]))

(def cli-options
  [["-d" "--dir DIR" "Template directory. Can be relative or absolute."
    :validate [#(fs/exists? %) "Directory must be valid."]]
   ["-m" "--model MODEL" "Path to XML model to use. Can be relative or absolute."
    :id :model-paths
    :default []
    :validate [#(fs/exists? %) "Model must be valid."]
    :assoc-fn (fn [opts opt v] (update opts opt conj v))]
   ["-o" "--output DIR" "Where to output the templates. Can be relative or absolute."
    :id :output-dir]
   ["-v" nil "Verbosity level; may be specified multiple times to increase value"
    ;; If no long-option is specified, an option :id must be given
    :id :verbosity
    :default 0
    ;; Use :update-fn to create non-idempotent options (:default is applied first)
    :update-fn inc]
   ["-D" "--[no-]daemon" "Detach the process" :default true]
   ["-w" "--watch" "Watch the given template directory"
    :id :watch?
    :default false]
   ["-h" "--help"]])

(defn usage [options-summary]
  (->> ["Usage: program-name [options] action"
        ""
        "Options:"
        options-summary
        ""
        "Actions:"
        "  generate    Generate everything inside the provided template directory"
        "  validate    Validate everything inside the provided template directory"
        ""
        "Please refer to the manual page for more information."]
       (string/join \newline)))

(defn error-msg [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (string/join \newline errors)))

(defn validate-args
  "Validate command line arguments. Either return a map indicating the program
  should exit (with a error message, and optional ok status), or a map
  indicating the action the program should take and the options provided."
  [args]
  (let [{:keys [options arguments errors summary]} (cli/parse-opts args cli-options)]
    (cond
      (:help options)
      {:exit-message (usage summary) :ok? true}
      errors
      {:exit-message (error-msg errors)}
      ;; custom validation on arguments
      (and (= 1 (count arguments))
           (#{"generate" "validate"} (first arguments)))
      {:action (first arguments) :options options}
      :else
      {:exit-message (usage summary)})))

(defn exit [status msg]
  (println msg)
  (System/exit status))

(defn -main [& args]
  (let [{:keys [action options exit-message ok?]} (validate-args args)]
    (if exit-message
      (exit (if ok? 0 1) exit-message)
      (case action
        "generate" (generate-all options)
        "validate" (println "Not yet!")))))

(comment
  (-main
    "-d" "resources/templates"
    "-m" "resources/templates/library.xml"
    "-o" "resources/new-new-gen"
    "generate"))