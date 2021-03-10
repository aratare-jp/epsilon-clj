(ns epsilon.core
  (:gen-class)
  (:require [puget.printer :refer [pprint]]
            [clojure.tools.cli :as cli]
            [clojure.string :as string]
            [epsilon.generator :refer [generate-all validate-all watch]]
            [me.raynes.fs :as fs]
            [taoensso.timbre :as log]))

(def cli-options
  [["-d" "--dir DIR" "Template directory. Can be relative or absolute."
    :id :template-dir
    :validate [#(and (fs/exists? %) (fs/directory? %)) "Directory must be valid."]]
   ["-m" "--model MODEL" "Path to XML model to use. Can be relative or absolute. Can be used multiple times."
    :id :model-paths
    :default []
    :validate [#(and (fs/exists? %) (fs/file? %)) "Model must be valid."]
    :assoc-fn (fn [opts opt v] (update opts opt conj v))]
   ["-o" "--output DIR" "Where to output the templates. Can be relative or absolute."
    :id :output-path]
   ["-v" nil "Verbosity level; may be specified up to 2 times. Levels: INFO (default) -> DEBUG -> TRACE"
    ;; If no long-option is specified, an option :id must be given
    :id :min-level
    :default 0
    :update-fn inc
    :validate [#(<= % 2) "Verbosity level must not exceed 2."]
    :post-validation true]
   ["-w" "--watch" "Watch the given template directory."
    :id :watch?
    :default false]
   ["-h" "--help" "Display help message."]])

(defn usage [options-summary]
  (->> ["Usage: program-name [options] action"
        ""
        "Options:"
        options-summary
        ""
        "Actions:"
        "  generate    Generate everything inside the provided template directory. This will validate first."
        "  validate    Validate everything inside the provided template directory"
        "  watch       Standalone watch mode. Will no trigger any validation or generation beforehand."
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
      (not= (count arguments) 1)
      {:exit-message "Only allow one argument."}
      (#{"generate" "validate" "watch"} (first arguments))
      (let [action (keyword (first arguments))]
        {:action  action
         :options (if (= :watch action) (assoc options :watch? true) options)})
      :else
      {:exit-message (usage summary)})))

(defn exit [status msg]
  (println msg)
  (System/exit status))

(def actions-map
  {:generate generate-all
   :validate validate-all
   :watch    watch})

(defn add-shutdown-hook [handler]
  "Add shutdown hook so we can properly exit all the file watchers."
  (-> (Runtime/getRuntime)
      (.addShutdownHook
        (new Thread
             (fn []
               (println "Exiting. Cleaning up all watchers.")
               (handler))))))

(defn filtered-log-middleware
  "This middleware will replace normal info logging with typical println so the user will get normal prompts in the
  CLI. Only works for info, will ignore any other types. Once the user turns on verbosity, will revert back to normal
  logging."
  [appender-data]
  (let [{:keys [level vargs]} appender-data]
    (if (or (= :info level) (= :error level))
      (println (string/join " " vargs))
      appender-data)))

(defn config-log
  "Configure Timbre. Will merge instead of replacing the default config."
  [{:keys [min-level]}]
  (let [log-level  (case min-level
                     0 :info
                     1 :debug
                     2 :trace)
        middleware (if (= min-level 0)
                     [filtered-log-middleware]
                     [])]
    (log/merge-config! {:min-level  log-level
                        :middleware middleware})))

(defn -main [& args]
  (let [{:keys [action options exit-message ok?]} (validate-args args)]
    (if exit-message
      (exit (if ok? 0 1) exit-message)
      (do
        (config-log options)
        (log/info "Welcome!")
        (let [result ((get actions-map action) options)]
          (try
            (if (:watch? options)
              (let [{:keys [handler future]} result]
                (add-shutdown-hook handler)
                (.get future)))
            (catch Exception _)
            (finally (shutdown-agents))))))))