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
          interval-ms 1000000
          org "org1"
          repo "repo1"
          last 1
          db (#'storage/start-rocksdb config)
          [stop-fn stop-chan] (#'sut/schedule-event-log interval-ms org repo last db config)]
      (try
        (t/is (= false (stop-fn)))
        (finally (#'storage/stop-rocksdb db)))))
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
        (finally (#'storage/stop-rocksdb db))))))

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
          expected-key (str org "/" repo)
          new-schedule (schedule interval-ms org repo 1)]
      (try
        (t/is (= interval-ms (:interval-ms new-schedule)))
        (t/is (t/function?  (:stop-fn new-schedule)))
        (t/is (satisfies? clojure.core.async.impl.protocols/Channel (:stop-chan new-schedule)))
        (finally
          ((:stop-fn new-schedule))
          (#'storage/stop-rocksdb db)))))
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
          expected-key (str org "/" repo)
          old-schedule (schedule interval-ms org repo 1)
          new-schedule (schedule interval-ms org repo 2)]
      (try
        (t/is (nil? (async/<!! (:stop-chan old-schedule))))
        (finally
          ((:stop-fn old-schedule)) ;; just to make sure
          ((:stop-fn new-schedule))
          (#'storage/stop-rocksdb db))))))
