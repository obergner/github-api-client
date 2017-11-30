(ns github-api-client.management-api
  (:require [compojure.core :as com]
            [compojure.route :as route]
            [ring.middleware.json :as mjson]
            [ring.logger :as alog]
            [ring.adapter.jetty :as jetty]
            [github-api-client.conf :as conf]
            [github-api-client.github-api :as api]
            [github-api-client.task :as task]
            [github-api-client.storage :as storage]
            [mount.core :as mount]
            [clojure.tools.logging :as log])
  (:import (org.eclipse.jetty.server Server)))

(defn- check-health
  [config]
  (let [commit-type-client (api/commit-type-client config)]
    (try
      (commit-type-client)
      {:status 200
       :body {:status "OK"}}
      (catch Exception e
        (log/errorf "Health check failed: %s" (.getMessage e))
        {:status 503
         :body {:status "Service Temporarily Unavailable"
                :message "GitHub API does not respond"}}))))

(defn- put-schedule
  [payload org repo schedules db config]
  (let [interval-ms (get payload "interval-ms")
        last (get payload "last")
        schedule (task/make-scheduler schedules db config)
        sched (schedule interval-ms org repo last)]
    (log/infof "RCVD: management API request to schedule [%s/%s last %d every %d ms]" org repo last interval-ms)
    {:status 201
     :headers {"Content-Type" "application/json"}
     :body {:interval-ms interval-ms
            :organization org
            :repository repo
            :last last}}))

(defn- delete-schedule
  [org repo schedules]
  (log/infof "RCVD: management API request to delete schedule [%s/%s]" org repo)
  (if (task/cancel-schedule org repo schedules)
    {:status 200
     :headers {"Content-Type" "application/json"}
     :body {:status 200
            :message "DELETED"}}
    {:status 404
     :headers {"Content-Type" "application/json"}
     :body {:status 404
            :message "Not Found"}}))

(defn- management-api-routes
  "Define and return management API `compojure` routes, as a `Ring` handler. Use `config` hash to create and configure
  `github-api-client.github-api` clients."
  [config schedules db]
  (com/routes
   (com/GET "/health" []
     (check-health config))
   (com/PUT "/schedules/:org/:repo" [org repo :as {payload :body}]
     (put-schedule payload org repo schedules db config))
   (com/DELETE "/schedules/:org/:repo" [org repo]
     (delete-schedule org repo schedules))
   (route/not-found
    {:status 404
     :message "Resource not found"})))

(defn- management-api-app
  "Define and return management API `Ring` application, using `config` hash to configure`github-api-client.github-api`
  clients."
  [config schedules db]
  (-> (management-api-routes config schedules db)
      alog/wrap-with-logger
      mjson/wrap-json-response
      mjson/wrap-json-body))

(defn- ^Server start-management-api
  "Start an embedded `Jetty` instance serving our management API, using the `config` hash to configure port and access
  to GitHub API. Return an `org.eclipse.jetty.server.Server` instance that may be stopped by calling `.stop` on it."
  [{:keys [management-api-port], :as config} schedules db]
  (log/infof "Starting management API on port [%s], using config [%s] ..." management-api-port config)
  (let [management-api (jetty/run-jetty (management-api-app config schedules db) {:port management-api-port :join? false})]
    management-api))

(defn- stop-management-api
  "Stop `server`, an `org.eclipse.jetty.server.Server` instance. Return `nil`."
  [^Server server]
  (log/infof "Stopping management API ...")
  (.stop server))

(mount/defstate management-api
  :start (start-management-api conf/conf task/schedules storage/db)
  :stop (stop-management-api management-api))
