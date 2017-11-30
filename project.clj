(defproject github-api-client "1.0.0-SNAPSHOT"
  :description "A demo client for GitHub's GraphQL API"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :plugins [[lein-environ "1.1.0"]
            [lein-cljfmt "0.5.7"]
            [lein-marginalia "0.9.1"]]
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/core.async "0.3.465"]
                 [mount "0.1.11"]
                 [tolitius/mount-up "0.1.1"]
                 [environ "1.1.0"]
                 [cheshire "5.8.0"]
                 [clj-http "3.7.0"]
                 [org.rocksdb/rocksdbjni "5.8.6"]
                 [compojure "1.6.0"]
                 [ring/ring-core "1.6.3"]
                 [ring/ring-jetty-adapter "1.6.3"]
                 [ring/ring-json "0.4.0"]
                 [ring-logger "0.7.7"]
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
  :env {:log-interval-ms "600000"
        :gh-api-url "https://api.github.com/graphql"
        :gh-org "tensorflow"
        :gh-repo "tensorflow"
        :gh-prs-last "5"
        :rocksdb-path "./targer/prod.db"
        :management-api-port "3100"}
  :profiles {:dev [:dev-public :dev-private]
             :dev-public {:source-paths ["profiles/dev/src"]
                          :resource-paths ["profiles/dev/resources"]
                          :dependencies [[se.haleby/stub-http "0.2.3"]
                                         [pjstadig/humane-test-output "0.8.3"]]
                          :plugins      [[com.jakemccrary/lein-test-refresh "0.21.1"]]
                          :repl-options {:init-ns user}
                          :injections [(require 'pjstadig.humane-test-output)
                                       (pjstadig.humane-test-output/activate!)]
                          :env {:log-interval-ms "180000"
                                :gh-api-url "https://api.github.com/graphql"
                                :gh-org "obergner"
                                :gh-repo "github-api-client"
                                :gh-prs-last "2"
                                :rocksdb-path "./target/dev.db"
                                :management-api-port "2200"}}
             :dev-private {:env {:gh-api-token "overridden-in-profile.clj"}}
             :test {:resource-paths ["test-resources"]
                    :env {:log-interval-ms "10000"
                          :gh-api-url "http://localhost:3000/graphql"
                          :gh-api-token "test-api-token"
                          :gh-org "test-org"
                          :gh-repo "test-repo"
                          :gh-prs-last "5"
                          :rocksdb-path "./target/test.db"
                          :management-api-port "2100"}}
             :uberjar {:aot :all}}
  :aliases {"doc" ["marg" "--dir" "./target/doc"]})
