(ns github-api-client.task
  (:require [github-api-client.event-log :as event-log]
            [schejulure.core :as sched]
            [clojure.tools.logging :as log]))

(def ^:private scheduled-event-log (atom nil))

(defn schedule-event-log
  "Start running even-log each minute if not yet started. Otherwise,
  do nothing. Use supplied `config` to initialise event logger.
  Return nil."
  [config]
  (swap! scheduled-event-log (fn [s]
                               (if (nil? s)
                                 (do
                                   (log/infof "Scheduling event logger to run each minute")
                                   (sched/schedule {:hour (range 0 24) :minute (range 0 60)}
                                                   #(let [log-prs (event-log/pull-requests-logger config)]
                                                      (log-prs (:gh-org config)
                                                               (:gh-repo config)
                                                               (:gh-prs-last config)))))
                                 s)))
  nil)

(defn cancel-event-log
  "Cancel event-log task if running. Otherwise, do nothing."
  []
  (when-let [task @scheduled-event-log]
    (log/infof "Canceling event logger ...")
    (future-cancel task)
    (swap! scheduled-event-log (constantly nil))
    (log/infof "[DONE] Event logger has been canceled")))
