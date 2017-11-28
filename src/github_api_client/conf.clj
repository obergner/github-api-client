(ns github-api-client.conf
  (:require [environ.core :refer [env]]))

(defn config
  "Read application configuration from environment variables and return
  it as a hash."
  []
  {:log-interval-ms (read-string (env :log-interval-ms))
   :gh-api-url (env :gh-api-url)
   :gh-api-token (env :gh-api-token)
   :gh-org (env :gh-org)
   :gh-repo (env :gh-repo)
   :gh-prs-last (read-string (env :gh-prs-last))
   :rocksdb-path (env :rocksdb-path)
   :management-api-port (read-string (env :management-api-port))})
