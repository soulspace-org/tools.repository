(defproject org.soulspace.clj/tools.repository "0.3.7-SNAPSHOT"
  :description "The tools.repository component provides tools to handle (maven) repositories and artifacts."
  :url "https://github.com/lsolbach/CljDevTools"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}

  ; use deps.edn dependencies
  ; :plugins [[lein-tools-deps "0.4.5"]]
  ; :middleware [lein-tools-deps.plugin/resolve-dependencies-with-deps-edn]
  ; :lein-tools-deps/config {:config-files [:install :user :project]}

  :dependencies [[org.clojure/clojure "1.11.1"]
                 [org.clojure/data.zip "1.0.0"]
                 [org.clojure/data.json "2.4.0"]
                 [org.clojure/data.csv "1.0.1"]
                 [org.clojure/data.xml "0.0.8"]
                 [http-kit/http-kit "2.5.3"]
                 [org.soulspace.clj/clj.java "0.9.1"]]
  
  :test-paths ["test"]

  :profiles {:dev {:dependencies [[org.clojure/test.check "1.1.1"]
                                  [djblue/portal "0.37.1"]
                                  [criterium "0.4.6"]
                                  [com.clojure-goes-fast/clj-java-decompiler "0.3.4"]
                                  [expound/expound "0.9.0"]]
                   :global-vars {*warn-on-reflection* true}}}

  :scm {:name "git" :url "https://github.com/soulspace-org/tools.repository"}
  :deploy-repositories [["clojars" {:sign-releases false :url "https://clojars.org/repo"}]])
