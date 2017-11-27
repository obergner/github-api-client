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
  (let [cnf (conf/config)]
    (app/log-startup-banner)
    (storage/start-rocksdb cnf)
    (let [[_ stop-chan] (task/schedule-event-log cnf)]
      (async/<!! stop-chan))))
