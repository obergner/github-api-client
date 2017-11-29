(ns github-api-client.core
  (:require [clojure.core.async :as async]
            [mount.core :as mount]
            [mount-up.core :as mu]
            [github-api-client.app :as app])
  (:gen-class))

(defn -main
  "Start application"
  [& args]
  (mu/on-upndown :info mu/log :before)
  (mount/start)
  (let [{:keys [stop-chan]} app/app]
    (async/<!! stop-chan)))
