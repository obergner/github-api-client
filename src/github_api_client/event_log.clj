(ns github-api-client.event-log
  (:require [github-api-client.storage :as storage]
            [github-api-client.github-api :as api]
            [cheshire.core :as json]
            [clojure.tools.logging :as log]))

(defn log-last-pull-requests
  "Retrieve `last` pull requests in repository `repo`
  owned by organization `org` and log them in our DB.
  Return key pull requests were stored under."
  [org repo last]
  (log/infof "Storing last [%d] pull requests in [organization: %s|repo: %s] in event log ..." last org last)
  (let [pull-requests (api/pull-request-info org repo last)
        key (str "pr:" org ":" repo ":" (System/currentTimeMillis))
        value (json/generate-string pull-requests)]
    (storage/put key value)
    (log/infof "[DONE] Stored [%d] pull requests under key [%s]" (count pull-requests) key)
    key))
