(defproject github-api-client "1.0.0-SNAPSHOT"
  :description "A demo client for GitHub's GraphQL API"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :plugins [[lein-environ "1.1.0"]]
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/core.async "0.3.465"]
                 [clj-http "3.7.0"]
                 [cheshire "5.8.0"]
                 [environ "1.1.0"]
                 [org.rocksdb/rocksdbjni "5.8.6"]
                 [schejulure "1.0.1"]
                 [ring/ring-jetty-adapter "1.6.3"]
                 [org.clojure/tools.logging "0.4.0"]
                 [org.slf4j/slf4j-api "1.7.25"]
                 [ch.qos.logback/logback-classic "1.2.3"]]
  :main ^:skip-aot github-api-client.core
  :target-path "target/%s"
  :manifest {"Application-Name" ~#(:name % "UNKNOWN")
             "Application-Version" ~#(:version % "UNKNOWN")
             "Application-Description" ~#(:description % "UNKNOWN")
             "Git-Branch" ~#(clojure.string/trim (:out (clojure.java.shell/sh "git" "rev-parse" "--abbrev-ref" "HEAD") %))
             "Git-Commit" ~#(clojure.string/trim (:out (clojure.java.shell/sh "git" "rev-parse" "HEAD") %))
             "Git-Dirty" ~#(str (not (empty? (clojure.string/trim (:out (clojure.java.shell/sh "git" "status" "--porcelain") %)))))}
  :env {:gh-api-url "https://api.github.com/graphql"
        :gh-org "tensorflow"
        :gh-repo "tensorflow"
        :gh-prs-last "5"
        :rocksdb-path "./.prod.db"}
  :profiles {:dev [:dev-public :dev-private]
             :dev-public {:env {:gh-api-url "https://api.github.com/graphql"
                                :gh-org "tensorflow"
                                :gh-repo "tensorflow"
                                :gh-prs-last "5"
                                :rocksdb-path "./target/dev.db"}
                          :resource-paths ["test-resources"]}
             :dev-private {:env {:gh-api-token "overridden-in-profile.clj"}}
             :test {:env {:gh-api-url "http://localhost:3000/graphql"
                          :gh-api-token "test-api-token"
                          :gh-org "tensorflow"
                          :gh-repo "tensorflow"
                          :gh-prs-last "5"
                          :rocksdb-path "./target/test.db"}}
             :uberjar {:aot :all}})
