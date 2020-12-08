(defproject altio "0.1.0-SNAPSHOT"

  :description "MDSD code generator"
  :url "http://github.com/aratare-tech/altio-core"

  :dependencies [[mount "0.1.16"]
                 [cprop "0.1.16"]
                 [org.clojure/clojure "1.10.1"]
                 [org.clojure/tools.namespace "0.2.11"]
                 [org.clojure/data.json "0.2.6"]
                 [org.clojure/tools.cli "1.0.194"]
                 [org.clojure/tools.logging "1.0.0"]
                 [org.antlr/antlr4-runtime "4.9"]]

  :min-lein-version "2.0.0"

  :source-paths ["src/clj"]
  :java-source-paths ["src/java"]
  :repl-options {:init-ns user
                 :timeout 120000}

  :profiles
  {:uberjar      {:omit-source   true
                  :prep-tasks    ["compile"]
                  :aot           :all
                  :uberjar-name  "altio.jar"
                  :target-path   "target/%s/"
                  :main          ^:skip-aot altio.core
                  :clean-targets ^{:protect false} [:target-path]}

   :project/test {:plugins        [[com.jakemccrary/lein-test-refresh "0.24.1"]
                                   [jonase/eastwood "0.3.5"]]
                  :resource-paths ["test/resources"]
                  :injections     [(require 'pjstadig.humane-test-output)
                                   (pjstadig.humane-test-output/activate!)]}})
