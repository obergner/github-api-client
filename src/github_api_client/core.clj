(ns github-api-client.core
  (:require [clojure.core.async :as async]
            [github-api-client.app :as app])
  (:gen-class))

(defn -main
  "Start application"
  [& args]
  (async/<!! (app/start args)))
