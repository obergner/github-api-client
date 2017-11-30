(ns github-api-client.management-api-test
  (:require [github-api-client.management-api :as sut]
            [clojure.test :as t]
            [clj-http.client :as http]
            [stub-http.core :as fake]
            [github-api-client.conf :as conf]
            [clojure.core.async :as async]
            [github-api-client.task :as task]
            [cheshire.core :as json]))

(defn- fix-gh-api-port
  [config new-port]
  (update-in config [:gh-api-url] (constantly (format "http://localhost:%d/graphql" new-port))))

(defn- cleanup
  [server schedules db]
  (#'sut/stop-management-api server)
  (doseq [[key {:keys [stop-fn]}] @schedules]
    (stop-fn))
  (async/close! db))

(t/deftest start-stop-management-api
  (t/testing "that /health endpoint returns 200/OK if GitHub API returns 200/OK"
    (fake/with-routes!
      {"/graphql" {:status 200
                   :headers {}
                   :body "{\"data\": {\"__type\": {\"kind\": \"OBJECT\"}}}"}}
      (let [config (fix-gh-api-port (conf/config) port)
            schedules (atom {})
            db (async/chan 10)
            server (#'sut/start-management-api config schedules db)]
        (try
          (t/is (= 200 (:status (http/get (format "http://localhost:%d/health" (:management-api-port config))))))
          (finally
            (cleanup server schedules db))))))
  (t/testing "that /health endpoint returns 503/Server Unavailable if GitHub API returns 500/Internal Server Error"
    (fake/with-routes!
      {"/graphql" {:status 500
                   :headers {}
                   :body ""}}
      (let [config (fix-gh-api-port (conf/config) port)
            schedules (atom {})
            db (async/chan 10)
            server (#'sut/start-management-api config schedules db)]
        (try
          (t/is (= 503 (:status (http/get (format "http://localhost:%d/health" (:management-api-port config)) {:throw-exceptions false}))))
          (finally
            (cleanup server schedules db))))))
  (t/testing "that PUT /schedules endpoint returns 201/Created in response to a well-formed PUT request"
    (fake/with-routes!
      {"/graphql" {:status 200
                   :headers {}
                   :body "{\"data\": {\"__type\": {\"kind\": \"OBJECT\"}}}"}}
      (let [interval-ms 100
            org "test-org1"
            repo "test-repo1"
            last 20
            config (fix-gh-api-port (conf/config) port)
            schedules (atom {})
            db (async/chan 10)
            server (#'sut/start-management-api config schedules db)]
        (try
          (t/is (= 201 (:status (http/put (format "http://localhost:%d/schedules/%s/%s" (:management-api-port config) org repo)
                                          {:throw-exceptions false
                                           :accept :json
                                           :content-type :json
                                           :body (format "{\"interval-ms\": %d, \"last\": %d}" interval-ms last)}))))
          (finally
            (cleanup server schedules db))))))
  (t/testing "that DELETE /schedules endpoint returns 404/Not Found if no matching schedule exists"
    (let [org "test-org2"
          repo "test-repo2"
          config {:management-api-port 54333}
          db (async/chan 10)
          schedules (atom {})
          server (#'sut/start-management-api config schedules db)]
      (try
        (t/is (= 404 (:status (http/delete (format "http://localhost:%d/schedules/%s/%s" (:management-api-port config) org repo)
                                           {:throw-exceptions false
                                            :accept :json}))))
        (finally
          (#'sut/stop-management-api server)
          (async/close! db)))))
  (t/testing "that DELETE /schedules endpoint returns 200/OK if matching schedule exists"
    (let [interval-ms 100
          org "test-org3"
          repo "test-repo3"
          last 20
          config {:management-api-port 45345}
          schedules (atom {})
          db (async/chan 10)
          schedule (task/make-scheduler schedules db config)
          sched (schedule interval-ms org repo last)
          server (#'sut/start-management-api config schedules db)]
      (try
        (t/is (= 200 (:status (http/delete (format "http://localhost:%d/schedules/%s/%s" (:management-api-port config) org repo)
                                           {:throw-exceptions false
                                            :accept :json}))))
        (finally
          (cleanup server schedules db)))))
  (t/testing "that GET /schedules endpoint returns list of all scheduled tasks"
    (let [interval-ms 100
          org "test-org4"
          repo "test-repo4"
          key (str org "/" repo)
          last 3
          config {:management-api-port 45345}
          schedules (atom {})
          db (async/chan 10)
          schedule (task/make-scheduler schedules db config)
          sched (schedule interval-ms org repo last)
          server (#'sut/start-management-api config schedules db)
          resp (http/get (format "http://localhost:%d/schedules" (:management-api-port config))
                         {:throw-exceptions false
                          :accept :json})
          body (json/parse-string (:body resp))]
      (try
        (t/is (= 200 (:status resp)))
        (t/is (= interval-ms (get-in body ["schedules" key "interval-ms"])))
        (t/is (= last (get-in body ["schedules" key "last"])))
        (finally
          (cleanup server schedules db))))))
