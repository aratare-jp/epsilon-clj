(defproject epsilon "1.0.0"
  :description "Model-driven code generator"
  :url "http://github.com/aratare-jp/epsilon-clj"
  :dependencies [[mount "0.1.16"]
                 [cprop "0.1.16"]
                 [org.clojure/clojure "1.10.1"]
                 [org.clojure/tools.cli "1.0.194"]
                 [org.clojure/tools.logging "1.0.0"]
                 [org.eclipse.epsilon/epsilon-core "1.5.1"]
                 [hawk "0.2.11"]
                 [mvxcvi/puget "1.3.1"]
                 [me.raynes/fs "1.4.6"]
                 [swiss-arrows "1.0.0"]
                 [com.taoensso/timbre "5.1.2"]
                 [org.clojure/core.async "1.3.610"]
                 [io.methvin/directory-watcher "0.14.0"]]
  :min-lein-version "2.0.0"
  :source-paths ["src/clj"]
  :java-source-paths ["src/java"]
  :resource-paths ["resources"]
  :profiles {:uberjar {:omit-source   true
                       :aot           :all
                       :target-path   "target/%s/"
                       :main          epsilon.core
                       :clean-targets ^{:protect false} [:target-path]}
             :test    {:plugins        [[lein-cloverage "1.2.1"]
                                        [lein-eftest "0.5.9"]]

                       :test-paths     ["test/clj"]
                       :dependencies   [[eftest "0.5.9"]]
                       :resource-paths ["test/resources"]}
             :dev     [:test
                       {:dependencies [[lein-virgil "0.1.9"]]}]})
