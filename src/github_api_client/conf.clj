(ns github-api-client.conf
  (:require [environ.core :refer [env]]))

(def config
  {:gh-api-url (env :gh-api-url)
   :gh-api-token (env :gh-api-token)})
