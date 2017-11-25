(ns github-api-client.conf
  (:require [environ.core :refer [env]]))

(def config
  {:gh-api-url (env :gh-api-url)
   :gh-api-token (env :gh-api-token)
   :gh-org (env :gh-org)
   :gh-repo (env :gh-repo)
   :gh-prs-last (read-string (env :gh-prs-last))
   :rocksdb-path (env :rocksdb-path)})
