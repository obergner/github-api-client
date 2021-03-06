(ns github-api-client.conf
  (:require [environ.core :refer [env]]
            [mount.core :as mount]))

(defn config
  "Read application configuration from environment variables and return it as a hash."
  []
  {:gh-api-url (env :gh-api-url)
   :gh-api-token (env :gh-api-token)
   :rocksdb-path (env :rocksdb-path)
   :management-api-port (read-string (env :management-api-port))})

(defn startup-params
  "Read startup parameters `log-interval-ms`, `gh-org`, `gh-repo`, `gh-prs-last` from environment variables and return
  them as a hash."
  []
  {:log-interval-ms (read-string (env :log-interval-ms))
   :gh-org (env :gh-org)
   :gh-repo (env :gh-repo)
   :gh-prs-last (read-string (env :gh-prs-last))})

(mount/defstate conf :start (config))

(mount/defstate params :start (startup-params))
