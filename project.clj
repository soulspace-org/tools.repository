(defproject org.soulspace.clj/tools.repository "0.3.7-SNAPSHOT"
  :description "The tools.repository component provides tools to handle (maven) repositories and artifacts."
  :url "https://github.com/lsolbach/CljDevTools"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}

  ; use deps.edn dependencies
  :plugins [[lein-tools-deps "0.4.5"]]
  :middleware [lein-tools-deps.plugin/resolve-dependencies-with-deps-edn]
  :lein-tools-deps/config {:config-files [:install :user :project]}

  :test-paths ["test"]

  :profiles {:dev {:dependencies [[org.clojure/test.check "1.1.1"]
                                  [djblue/portal "0.37.1"]
                                  [criterium "0.4.6"]]
                   :global-vars {*warn-on-reflection* true}}}

  :scm {:name "git" :url "https://github.com/soulspace-org/tools.repository"}
  :deploy-repositories [["clojars" {:sign-releases false :url "https://clojars.org/repo"}]])
