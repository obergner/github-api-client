(ns github-api-client.task-test
  (:require [github-api-client.task :as sut]
            [github-api-client.storage :as storage]
            [clojure.test :as t]
            [clojure.core.async :as async]))

(t/deftest schedule-event-log
  (t/testing "that function returned from schedule-event-log returns false when called"
    (let [config {:gh-api-url "test"
                  :gh-api-token "test"
                  :rocksdb-path "./target/.test1.db"}
          log-interval-ms 1000000
          gh-org "org"
          gh-repo "repo"
          gh-prs-last 1
          db (storage/start-rocksdb config)
          [stop-fn stop-chan] (sut/schedule-event-log log-interval-ms gh-org gh-repo gh-prs-last db config)]
      (try
        (t/is (= false (stop-fn)))
        (finally (storage/stop-rocksdb db)))))
  (t/testing "that channel returned from schedule-event-log publishes nil stop message"
    (let [config {:gh-api-url "test"
                  :gh-api-token "test"
                  :rocksdb-path "./target/.test2.db"}
          log-interval-ms 10
          gh-org "org"
          gh-repo "repo"
          gh-prs-last 1
          db (storage/start-rocksdb config)
          [stop-fn stop-chan] (sut/schedule-event-log log-interval-ms gh-org gh-repo gh-prs-last db config)]
      (try
        (stop-fn)
        (t/is (nil? (async/<!! stop-chan)))
        (finally (storage/stop-rocksdb db))))))

(t/deftest event-log-scheduler
  (t/testing "that function returned from event-log-scheduler returns updated schedules hash"
    (let [schedules (atom {})
          config {:gh-api-url "test"
                  :gh-api-token "test"
                  :rocksdb-path "./target/.test3.db"}
          db (storage/start-rocksdb config)
          schedule (sut/make-scheduler schedules db config)
          log-interval-ms 1000000
          gh-org "org"
          gh-repo "repo"
          expected-key (str gh-org "/" gh-repo)
          updated-schedules (schedule 1000000 gh-org gh-repo 1)]
      (try
        (t/is (= log-interval-ms (:log-interval-ms updated-schedules)))
        (t/is (t/function?  (:stop-fn updated-schedules)))
        (t/is (satisfies? clojure.core.async.impl.protocols/Channel (:stop-chan updated-schedules)))
        (finally
          ((:stop-fn updated-schedules))
          (storage/stop-rocksdb db))))))
