(ns github-api-client.task
  (:require [github-api-client.event-log :as event-log]
            [mount.core :as mount]
            [clojure.core.async :as async]
            [clojure.tools.logging :as log]))

(defn- cancel-schedules
  [schedules]
  (doseq [[key {:keys [stop-fn]}] @schedules]
    (stop-fn)))

(mount/defstate schedules
  :start (atom {})
  :stop (cancel-schedules schedules))

(defn- schedule-event-log
  "Schedule `github-api-client.event-log/pull-requests-logger` to run every `interval-ms` milliseconds,
  pulling the last `last` pull requests from GitHub repository `org`/`repo`, storing them in
  our database. Use configuration hash `config` to configure GitHub API URL, access token and so forth.

  Return `[stop-fn stop-chan]` where

   * `stop-fn` is a function that takes not arguments and cancels the scheduled event log when called, and
   * `stop-chan` is a `clojure.core.async` `channel` that publishes exactly one message when this event-logger
     shuts down.

  The purpose of `stop-chan` is for the main thread to block on it so that the application does not exit
  immediately."
  [interval-ms org repo last db config]
  (log/infof "START: log last [%d] pull requests in [%s/%s] every [%d] ms"
             last org repo interval-ms)
  (let [log-pull-requests (event-log/pull-requests-logger db config)
        should-run (atom true)
        stop-chan (async/go
                    (while @should-run
                      (async/<! (async/timeout interval-ms))
                      (when @should-run
                        (try
                          (log-pull-requests org repo last)
                          (catch Exception e
                            (log/errorf e "[IGNORED] Error while trying to log last [%d] pull requests in [%s/%s]: %s"
                                        last org repo (.getMessage e))))))
                    (log/infof "STOP: received request to stop event-log loop"))]
    [#(reset! should-run false) stop-chan]))

(defn make-scheduler
  "Take an `atom` `schedules` wrapping a `hash` and return a function that takes a `config`
  hash as its only argument.

  When called, the returned function will schedule `github-api-client.event-log/pull-request-logger` to run
  every `interval-ms` against the GitHub repository `org/repo`, on each run pulling the last
  `last` pull requests, storing them in our permanent storage. `interval-ms`, `org`,
  `repo` and `last` need to be passed in as arguments to the returned function.

  The returned function will return `{:interval-ms interval-ms :stop-fn stop-fn :stop-chan :stop-chan}`
  where

   * `stop-fn` is a function that takes not arguments and cancels the scheduled event log when called, and
   * `stop-chan` is a `clojure.core.async` `channel` that publishes exactly one message when this event-logger
     shuts down.

  Store `stop-fn` and `stop-chan` under key \"`org/repo`\" in `scheduled-event-logs`, our state holder."
  [schedules db config]
  (fn [interval-ms org repo last]
    (let [key (str org "/" repo)]
      (when-let [{:keys [stop-fn]} (get @schedules key)]
        (stop-fn))
      (let [[stop-fn stop-chan] (schedule-event-log interval-ms org repo last db config)
            new-schedule {:stop-fn stop-fn :stop-chan stop-chan :interval-ms interval-ms}]
        (get (swap! schedules #(update-in % [key] (constantly new-schedule))) key)))))
