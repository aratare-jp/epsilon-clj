(defproject org.clojars.aratare/epsilon "v1.0.0"
  :description "Model-driven code generator"
  :url "http://github.com/aratare-jp/epsilon-clj"
  :license "Apache 2.0"
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/tools.cli "1.0.194"]
                 [org.eclipse.epsilon/epsilon-core "1.5.1"]
                 [mvxcvi/puget "1.3.1"]
                 [me.raynes/fs "1.4.6"]
                 [medley "1.3.0"]
                 [com.taoensso/timbre "5.1.2"]
                 [com.fzakaria/slf4j-timbre "0.3.20"]
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
                       :dependencies   [[eftest "0.5.9"]
                                        [tortue/spy "2.4.0"]
                                        [expectations/clojure-test "1.2.1"]]
                       :resource-paths ["test/resources"]}
             :dev     [:test
                       {:dependencies [[lein-virgil "0.1.9"]]}]})
