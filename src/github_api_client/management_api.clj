(ns github-api-client.management-api
  (:require [compojure.core :as com]
            [compojure.route :as route]
            [ring.middleware.json :as mjson]
            [ring.logger :as alog]
            [ring.adapter.jetty :as jetty]
            [github-api-client.conf :as conf]
            [github-api-client.github-api :as api]
            [mount.core :as mount]
            [clojure.tools.logging :as log]))

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

(defn start-management-api
  "Start an embedded `Jetty` instance serving our management API, using the `config`
  hash to configure port and access to GitHub API.
  Return a function that takes no arguments and if called stops started `Jetty` instance."
  [{:keys [management-api-port], :as config}]
  (log/infof "Starting management API on port [%s], using config [%s] ..." management-api-port config)
  (let [management-api (jetty/run-jetty (management-api-app config) {:port management-api-port :join? false})]
    #(.stop management-api)))

(mount/defstate management-api
  :start (start-management-api conf/conf)
  :stop (management-api))
