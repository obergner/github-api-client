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
            [mount-up.core :as mu]))

(mu/on-upndown :info mu/log :before)

(println
 "
-----------------------------------------------------------------------------------------
Welcome to GitHub API Client's REPL. Here's an overview of some custom commands you
might find useful:

 * (start):
     start GitHub API Client, including all subsystems
 * (stop):
     stop GitHub API Client, taking care to stop all subsystems in reverse startup order
 * (restart):
     stop, then start again
 * (schedule interval-ms org repo last):
     schedule importing last `last` pull requests from GitHub repository `org`/`repo`
     every `interval-ms` milliseconds
 * (check-health):
     call GitHub API Client's /health endpoint
 * (get-schedules):
     GET call to GitHub API Client's /schedules endpoint, to get the list of all
     scheduled tasks
 * (put-schedule interval-ms org repo last):
     PUT call to GitHub API Client's /schedules endpoint, equivalent to (schedule ....)
     above
 * (delete-schedule org repo):
     DELETE call to GitHub API Client's /schedules endpoint 

Enjoy
-----------------------------------------------------------------------------------------
")

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

(defn get-schedules
  []
  (let [port (:management-api-port conf/conf)
        uri (format "http://localhost:%d/schedules" port)]
    (http/get uri {:throw-exceptions false
                   :accept :json})))

(defn put-schedule
  [interval-ms org repo last]
  (let [port (:management-api-port conf/conf)
        uri (format "http://localhost:%d/schedules/%s/%s" port org repo)]
    (http/put uri {:throw-exceptions false
                   :accept :json
                   :content-type :json
                   :body (format "{\"interval-ms\": %d, \"last\": %d}" interval-ms last)})))

(defn delete-schedule
  [org repo]
  (let [port (:management-api-port conf/conf)
        uri (format "http://localhost:%d/schedules/%s/%s" port org repo)]
    (http/delete uri {:throw-exceptions false
                      :accept :json})))
