(defproject github-api-client "1.0.0-SNAPSHOT"
  :description "A demo client for GitHub's GraphQL API"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :plugins [[lein-environ "1.1.0"]]
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [clj-http "3.7.0"]
                 [cheshire "5.8.0"]
                 [environ "1.1.0"]
                 [org.mapdb/mapdb "3.0.5" :exclusions [org.eclipse.collections/eclipse-collections
                                                       org.eclipse.collections/eclipse-collections-api
                                                       org.eclipse.collections/eclipse-collections-forkjoin
                                                       com.google.guava/guava]]
                 [org.eclipse.collections/eclipse-collections-api "7.1.1"]
                 [org.eclipse.collections/eclipse-collections "7.1.1"]
                 [org.eclipse.collections/eclipse-collections-forkjoin "7.1.1"]
                 [com.google.guava/guava "19.0"]
                 [schejulure "1.0.1"]
                 [ring/ring-jetty-adapter "1.6.3"]
                 [org.clojure/tools.logging "0.4.0"]
                 [org.slf4j/slf4j-api "1.7.25"]
                 [ch.qos.logback/logback-classic "1.2.3"]]
  :main ^:skip-aot github-api-client.core
  :target-path "target/%s"
  :manifest {"Git-Branch" ~#(clojure.string/trim (:out (clojure.java.shell/sh "git" "rev-parse" "--abbrev-ref" "HEAD") %))
             "Git-Commit" ~#(clojure.string/trim (:out (clojure.java.shell/sh "git" "rev-parse" "HEAD") %))
             "Git-Dirty" ~#(str (not (empty? (clojure.string/trim (:out (clojure.java.shell/sh "git" "status" "--porcelain") %)))))}
  :profiles {:dev [:dev-public :dev-private]
             :dev-public {:env {:gh-api-url "https://api.github.com/graphql"}
                          :resource-paths ["test-resources"]}
             :dev-private {:env {:gh-api-token "overridden-in-profile.clj"}}
             :test {:env {:gh-api-url "http://localhost:3000/graphql"
                          :gh-api-token "test-api-token"}}
             :uberjar {:aot :all}})
