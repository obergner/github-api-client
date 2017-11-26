(ns github-api-client.task-test
  (:require [github-api-client.task :as sut]
            [clojure.test :as t]
            [clojure.core.async :as async]))

(t/deftest schedule-event-log
  (t/testing "that function returned from schedule-event-log returns false when called"
    (let [config {:log-interval-ms 1000000
                  :gh-api-url "test"
                  :gh-api-token "test"
                  :gh-org "org"
                  :gh-repo "repo"
                  :gh-prs-last 1}
          [stop-fn stop-chan] (sut/schedule-event-log config)]
      (t/is (= false (stop-fn)))))
  (t/testing "that channel returned from schedule-event-log publishes nil stop message"
    (let [config {:log-interval-ms 1000000
                  :gh-api-url "test"
                  :gh-api-token "test"
                  :gh-org "org"
                  :gh-repo "repo"
                  :gh-prs-last 1}
          [stop-fn stop-chan] (sut/schedule-event-log config)]
      (stop-fn)
      (t/is (nil? (async/<!! stop-chan))))))

