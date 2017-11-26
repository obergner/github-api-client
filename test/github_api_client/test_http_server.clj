(ns github-api-client.test-http-server
  (:require [ring.adapter.jetty :as ring]
            [cheshire.core :as json]
            [clojure.string :as string]
            [clojure.tools.logging :as log]))

(def ^:private mock-server (atom nil))

(def ^:private expectation (atom nil))

(def ^:private default-response
  {:status 503
   :body (json/generate-string {:test-status "failed"})})

(defn when-then
  "Tell mock HTTP server to next expect an HTTP request matching
  `request-pred`, a function that takes the received `Ring` request
  and returns either `true` or `false`.
  If the next received request matches return `Ring` response `success`."
  [request-pred success]
  (reset! expectation {:pred request-pred :resp success}))

(defn- mock-handler
  [ring-request]
  (let [{:keys [pred resp]} @expectation]
    (log/debugf "RCVD: %s" ring-request)
    (if (pred ring-request)
      resp
      default-response)))

(defn with-mock-server
  [test-fn]
  (swap! mock-server
         (fn [server]
           (if (some? server)
             (.stop server))
           (ring/run-jetty #'mock-handler {:port 3000 :join? false})))
  (test-fn)
  (.stop @mock-server))
