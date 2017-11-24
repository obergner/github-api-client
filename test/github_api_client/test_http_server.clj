(ns github-api-client.test-http-server
  (:require [ring.adapter.jetty :as ring]
            [cheshire.core :as json]
            [clojure.string :as string]
            [clojure.java.io :as io]))

(def ^:private mock-server (atom nil))

(def ^:private last-req (atom nil))

(def ^:private next-resp (atom nil))

(defn respond-with
  "Instruct mock HTTP server to respond with 'response'
  to next received request"
  [response]
  (swap! next-resp (constantly response)))

(defn last-request
  "Return last request received by mock HTTP server"
  []
  (let [lst-req @last-req]
    (swap! last-req (constantly nil))
    lst-req))

(defn- mock-handler
  [ring-request]
  (let [nxt-resp @next-resp]
    (swap! next-resp (constantly nil))
    (swap! last-req (constantly ring-request))
    (condp = [(:request-method ring-request) (:uri ring-request)]
      [:post "/graphql"] {:status (:status nxt-resp)
                          :headers (:headers nxt-resp)
                          :body (json/generate-string (:body nxt-resp))}
      {:status 404 :body ""})))

(defn with-mock-server
  [test-fn]
  (swap! mock-server
         (fn [server]
           (if (some? server)
             (.stop server))
           (ring/run-jetty #'mock-handler {:port 3000 :join? false})))
  (test-fn)
  (.stop @mock-server))
