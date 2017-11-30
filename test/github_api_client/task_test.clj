(ns github-api-client.task-test
  (:require [github-api-client.task :as sut]
            [github-api-client.storage :as storage]
            [clojure.test :as t]
            [clojure.core.async :as async]))

(defn- cleanup
  [db & scheduled-tasks]
  (doseq [{:keys [stop-fn]} scheduled-tasks]
    (stop-fn))
  (#'storage/stop-rocksdb db)
  (async/close! db))

(t/deftest schedule-event-log
  (t/testing "that function returned from schedule-event-log returns false when called"
    (let [config {:gh-api-url "test"
                  :gh-api-token "test"
                  :rocksdb-path "./target/.test1.db"}
          interval-ms 1000000
          org "org1"
          repo "repo1"
          last 1
          db (#'storage/start-rocksdb config)
          [stop-fn stop-chan] (#'sut/schedule-event-log interval-ms org repo last db config)]
      (try
        (t/is (= false (stop-fn)))
        (finally
          (cleanup db)))))
  (t/testing "that channel returned from schedule-event-log publishes nil stop message"
    (let [config {:gh-api-url "test"
                  :gh-api-token "test"
                  :rocksdb-path "./target/.test2.db"}
          interval-ms 10
          org "org2"
          repo "repo2"
          last 1
          db (#'storage/start-rocksdb config)
          [stop-fn stop-chan] (#'sut/schedule-event-log interval-ms org repo last db config)]
      (try
        (stop-fn)
        (t/is (nil? (async/<!! stop-chan)))
        (finally
          (cleanup db))))))

(t/deftest make-scheduler
  (t/testing "that function returned from make-scheduler returns hash containing proper schedule info"
    (let [schedules (atom {})
          config {:gh-api-url "test"
                  :gh-api-token "test"
                  :rocksdb-path "./target/.test3.db"}
          db (#'storage/start-rocksdb config)
          schedule (sut/make-scheduler schedules db config)
          interval-ms 1000000
          org "org3"
          repo "repo3"
          last 3
          new-schedule (schedule interval-ms org repo last)]
      (try
        (t/is (= interval-ms (:interval-ms new-schedule)))
        (t/is (= last (:last new-schedule)))
        (t/is (t/function?  (:stop-fn new-schedule)))
        (t/is (satisfies? clojure.core.async.impl.protocols/Channel (:stop-chan new-schedule)))
        (finally
          (cleanup db new-schedule)))))
  (t/testing "that schedule function stops previous schedule when overwriting it"
    (let [schedules (atom {})
          config {:gh-api-url "test"
                  :gh-api-token "test"
                  :rocksdb-path "./target/.test4.db"}
          db (#'storage/start-rocksdb config)
          schedule (sut/make-scheduler schedules db config)
          interval-ms 10
          org "org4"
          repo "repo4"
          old-schedule (schedule interval-ms org repo 1)
          new-schedule (schedule interval-ms org repo 2)]
      (try
        (t/is (nil? (async/<!! (:stop-chan old-schedule))))
        (finally
          (cleanup db old-schedule new-schedule))))))

(t/deftest cancel-schedule
  (t/testing "that cancel-schedule returns true if there is a matching scheduled task"
    (let [schedules (atom {})
          config {:gh-api-url "test"
                  :gh-api-token "test"
                  :rocksdb-path "./target/.test3.db"}
          db (#'storage/start-rocksdb config)
          schedule (sut/make-scheduler schedules db config)
          interval-ms 1000000
          org "cancel-schedule-org1"
          repo "cancel-schedule-repo1"
          new-schedule (schedule interval-ms org repo 1)]
      (try
        (t/is (= true (sut/cancel-schedule org repo schedules)))
        (finally
          (cleanup db new-schedule)))))
  (t/testing "that cancel-schedule returns false if there is no matching scheduled task"
    (let [schedules (atom {})
          org "cancel-schedule-org2"
          repo "cancel-schedule-repo2"]
      (t/is (= false (sut/cancel-schedule org repo schedules))))))
