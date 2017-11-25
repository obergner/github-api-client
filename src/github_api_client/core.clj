(ns github-api-client.core
  (:require [github-api-client.task :as task])
  (:gen-class))

(defn -main
  "Start application"
  [& args]
  (task/schedule-event-log))
