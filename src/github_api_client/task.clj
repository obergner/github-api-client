(ns github-api-client.task
  (:require [github-api-client.event-log :as event-log]
            [mount.core :as mount]
            [clojure.core.async :as async]
            [clojure.tools.logging :as log]))

(defn cancel-schedules
  [schedules]
  (doseq [[key {:keys [stop-fn]}] @schedules]
    (stop-fn)))

(mount/defstate schedules
  :start (atom {})
  :stop (cancel-schedules schedules))

(defn schedule-event-log
  "Schedule `github-api-client.event-log/pull-requests-logger` to run every `log-interval-ms` milliseconds,
  pulling the last `gh-prs-last` pull requests from GitHub repository `gh-org`/`gh-repo`, storing them in
  our database. Use configuration hash `config` to configure GitHub API URL, access token and so forth.

  Return `[stop-fn stop-chan]` where

   * `stop-fn` is a function that takes not arguments and cancels the scheduled event log when called, and
   * `stop-chan` is a `clojure.core.async` `channel` that publishes exactly one message when this event-logger
     shuts down.

  The purpose of `stop-chan` is for the main thread to block on it so that the application does not exit
  immediately."
  [log-interval-ms gh-org gh-repo gh-prs-last db config]
  (log/infof "START: log last [%d] pull requests in [%s/%s] every [%d] ms"
             gh-prs-last gh-org gh-repo log-interval-ms)
  (let [log-pull-requests (event-log/pull-requests-logger db config)
        should-run (atom true)
        stop-chan (async/go
                    (while @should-run
                      (async/<! (async/timeout log-interval-ms))
                      (when @should-run
                        (try
                          (log-pull-requests gh-org gh-repo gh-prs-last)
                          (catch Exception e
                            (log/errorf e "[IGNORED] Error while trying to log last [%d] pull requests in [%s/%s]: %s"
                                        gh-prs-last gh-org gh-repo (.getMessage e))))))
                    (log/infof "STOP: received request to stop event-log loop"))]
    [#(reset! should-run false) stop-chan]))

(defn make-scheduler
  "Take an `atom` `schedules` wrapping a `hash` and return a function that takes a `config`
  hash as its only argument.

  When called, the returned function will schedule `github-api-client.event-log/pull-request-logger` to run
  every `log-interval-ms` against the GitHub repository `gh-org/gh-repo`, on each run pulling the last
  `gh-prs-last` pull requests, storing them in our permanent storage. `log-interval-ms`, `gh-org`,
  `gh-repo` and `gh-prs-last` need to be passed in as arguments to the returned function.

  The returned function will return `{:log-interval-ms log-interval-ms :stop-fn stop-fn :stop-chan :stop-chan}`
  where

   * `stop-fn` is a function that takes not arguments and cancels the scheduled event log when called, and
   * `stop-chan` is a `clojure.core.async` `channel` that publishes exactly one message when this event-logger
     shuts down.

  Store `stop-fn` and `stop-chan` under key \"`gh-org/gh-repo`\" in `scheduled-event-logs`, our state holder."
  [schedules db config]
  (fn [log-interval-ms gh-org gh-repo gh-prs-last]
    (let [key (str gh-org "/" gh-repo)]
      (when-let [{:keys [stop-fn]} (get @schedules key)]
        (stop-fn))
      (let [[stop-fn stop-chan] (schedule-event-log log-interval-ms gh-org gh-repo gh-prs-last db config)
            new-schedule {:stop-fn stop-fn :stop-chan stop-chan :log-interval-ms log-interval-ms}]
        (get (swap! schedules #(update-in % [key] (constantly new-schedule))) key)))))
