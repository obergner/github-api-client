(ns github-api-client.core
  (:require [github-api-client.app :as app]
            [github-api-client.task :as task]
            [github-api-client.storage :as storage]
            [github-api-client.conf :as conf]
            [clojure.core.async :as async])
  (:gen-class))

(defn -main
  "Start application"
  [& args]
  (let [cfg (conf/config)
        {:keys [log-interval-ms gh-org gh-repo gh-prs-last]} (conf/startup-params)]
    (app/log-startup-banner "github_api_client.core")
    (storage/start-rocksdb cfg)
    (let [[_ stop-chan] (task/schedule-event-log log-interval-ms gh-org gh-repo gh-prs-last cfg)]
      (async/<!! stop-chan))))
