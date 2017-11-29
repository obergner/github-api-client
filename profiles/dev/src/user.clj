(ns user
  (:require [github-api-client.app :as app]
            [github-api-client.conf :as conf]
            [github-api-client.github-api :as github-api]
            [github-api-client.storage :as storage]
            [github-api-client.event-log :as event-log]
            [github-api-client.task :as task]
            [github-api-client.management-api :as management-api]
            [mount.core :as mount]
            [mount-up.core :as mu]))

(mu/on-upndown :info mu/log :before)

(defn start
  []
  (mount/start))

(defn stop
  []
  (mount/stop))

(defn restart
  []
  (mount/stop)
  (mount/start))

(defn schedule
  [log-interval-ms gh-org gh-repo gh-prs-last]
  (let [[stop-fn stop-chan]
        (#'task/schedule-event-log log-interval-ms gh-org gh-repo gh-prs-last storage/db conf/conf)]
    stop-fn))

