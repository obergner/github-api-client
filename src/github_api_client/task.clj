(ns github-api-client.task
  (:require [github-api-client.event-log :as event-log]
            [clojure.core.async :as async]
            [clojure.tools.logging :as log]))

(defn schedule-event-log
  "Schedule `github-api-client.event-log/pull-requests-logger` to run every `log-interval-ms` milliseconds,
  where `log-interval-ms` is the value associated with `:log-interval-ms` in the supplied configuration hash.

  Return `[stop-fn stop-chan]` where

   * `stop-fn` is a function that takes not arguments and cancels the scheduled event log when called, and
   * `stop-chan` is a `clojure.core.async` `channel` that publishes exactly one message when this event-logger
     shuts down.

  The purpose of `stop-chan` is for the main thread to block on it so that the application does not exit
  immediately."
  [{:keys [log-interval-ms gh-org gh-repo gh-prs-last], :as config}]
  (log/infof "START: log last [%d] pull requests in [%s/%s] every [%d] ms"
             gh-prs-last gh-org gh-repo log-interval-ms)
  (let [log-pull-requests (event-log/pull-requests-logger config)
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
