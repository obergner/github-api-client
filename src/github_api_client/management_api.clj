(ns github-api-client.management-api
  (:require [compojure.core :as com]
            [compojure.route :as route]
            [ring.middleware.json :as mjson]
            [ring.logger :as alog]
            [ring.adapter.jetty :as jetty]
            [github-api-client.conf :as conf]
            [github-api-client.github-api :as api]
            [mount.core :as mount]
            [clojure.tools.logging :as log])
  (:import (org.eclipse.jetty.server Server)))

(defn- management-api-routes
  "Define and return management API `compojure` routes, as a `Ring` handler. Use `config` hash
  to create and configure `github-api-client.github-api` clients."
  [config]
  (let [commit-type-client (api/commit-type-client config)]
    (com/routes
     (com/GET "/health" []
       (try
         (commit-type-client)
         {:status 200
          :body {:status "OK"}}
         (catch Exception e
           (log/errorf "Health check failed: %s" (.getMessage e))
           {:status 503
            :body {:status "Service Temporarily Unavailable"
                   :message "GitHub API does not respond"}})))
     (route/not-found
      {:status 404
       :message "Resource not found"}))))

(defn- management-api-app
  "Define and return management API `Ring` application, using `config` hash to configure
  `github-api-client.github-api` clients."
  [config]
  (-> (management-api-routes config)
      alog/wrap-with-logger
      mjson/wrap-json-response
      mjson/wrap-json-body))

(defn- ^Server start-management-api
  "Start an embedded `Jetty` instance serving our management API, using the `config`
  hash to configure port and access to GitHub API.
  Return an `org.eclipse.jetty.server.Server` instance that may be stopped by calling `.stop` on it."
  [{:keys [management-api-port], :as config}]
  (log/infof "Starting management API on port [%s], using config [%s] ..." management-api-port config)
  (let [management-api (jetty/run-jetty (management-api-app config) {:port management-api-port :join? false})]
    management-api))

(defn- stop-management-api
  "Stop `server`, an `org.eclipse.jetty.server.Server` instance. Return `nil`."
  [^Server server]
  (log/infof "Stopping management API ...")
  (.stop server))

(mount/defstate management-api
  :start (start-management-api conf/conf)
  :stop (stop-management-api management-api))
