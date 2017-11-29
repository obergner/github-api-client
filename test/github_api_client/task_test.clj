(ns github-api-client.task-test
  (:require [github-api-client.task :as sut]
            [clojure.test :as t]
            [clojure.core.async :as async]))

(t/deftest schedule-event-log
  (t/testing "that function returned from schedule-event-log returns false when called"
    (let [config {:gh-api-url "test"
                  :gh-api-token "test"}
          log-interval-ms 1000000
          gh-org "org"
          gh-repo "repo"
          gh-prs-last 1
          [stop-fn stop-chan] (sut/schedule-event-log log-interval-ms gh-org gh-repo gh-prs-last config)]
      (t/is (= false (stop-fn)))))
  (t/testing "that channel returned from schedule-event-log publishes nil stop message"
    (let [config {:gh-api-url "test"
                  :gh-api-token "test"}
          log-interval-ms 1000000
          gh-org "org"
          gh-repo "repo"
          gh-prs-last 1
          [stop-fn stop-chan] (sut/schedule-event-log log-interval-ms gh-org gh-repo gh-prs-last config)]
      (stop-fn)
      (t/is (nil? (async/<!! stop-chan))))))

(t/deftest event-log-scheduler
  (t/testing "that function returned from event-log-scheduler returns updated schedules hash"
    (let [schedules (atom {})
          config {:gh-api-url "test" :gh-api-token "test"}
          schedule (sut/event-log-scheduler schedules config)
          log-interval-ms 1000000
          gh-org "org"
          gh-repo "repo"
          expected-key (str gh-org "/" gh-repo)
          updated-schedules (schedule 1000000 gh-org gh-repo 1)]
      (try
        (println updated-schedules)
        (t/is (= log-interval-ms (:log-interval-ms updated-schedules)))
        (t/is (t/function?  (:stop-fn updated-schedules)))
        (t/is (satisfies? clojure.core.async.impl.protocols/Channel (:stop-chan updated-schedules)))
        (finally ((:stop-fn updated-schedules)))))))
