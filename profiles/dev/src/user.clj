(ns user
  (:require [github-api-client.app :as app]
            [github-api-client.conf :as conf]
            [github-api-client.github-api :as github-api]
            [github-api-client.storage :as storage]
            [github-api-client.event-log :as event-log]
            [github-api-client.task :as task]
            [clj-http.client :as http]
            [github-api-client.management-api :as management-api]
            [mount.core :as mount]
            [mount-up.core :as mu]
            [cheshire.core :as json]))

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
  [interval-ms org repo last]
  (let [do-schedule (task/make-scheduler task/schedules storage/db conf/conf)
        sched (do-schedule interval-ms org repo last)]
    sched))

(defn check-health
  []
  (let [port (:management-api-port conf/conf)
        uri (format "http://localhost:%d/health" port)]
    (http/get uri {:accept :json
                   :throw-exceptions false})))

(defn put-schedule
  [interval-ms org repo last]
  (let [port (:management-api-port conf/conf)
        uri (format "http://localhost:%d/schedules/%s/%s" port org repo)]
    (http/put uri {:throw-exceptions false
                   :accept :json
                   :content-type :json
                   :body (format "{\"interval-ms\": %d, \"last\": %d}" interval-ms last)})))

