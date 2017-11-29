(ns github-api-client.event-log
  (:require [github-api-client.storage :as storage]
            [github-api-client.github-api :as api]
            [cheshire.core :as json]
            [clojure.tools.logging :as log]))

(defn pull-requests-logger
  "Using supplied `db` and `config` create and return a function that takes
  organization `org`, repository `repo` and number `last` and
  looks up the latest `last` pull requests in `repo` belonging tools
  `organization`, storing them in our event log."
  [db config]
  (fn [org repo last]
    (log/infof "Storing last [%d] pull requests in [organization: %s|repo: %s] in event log ..." last org repo)
    (let [pull-request-info (api/request-info-client config)
          pull-requests (pull-request-info org repo last)
          key (str "pr:" org ":" repo ":" (System/currentTimeMillis))
          value (json/generate-string pull-requests)]
      (storage/put db key value)
      (log/infof "[DONE] Stored [%d] pull requests under key [%s]" (count pull-requests) key)
      key)))
