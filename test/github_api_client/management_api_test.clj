(ns github-api-client.management-api-test
  (:require [github-api-client.management-api :as sut]
            [clojure.test :as t]
            [clj-http.client :as http]
            [stub-http.core :as fake]
            [github-api-client.conf :as conf]))

(defn- fix-gh-api-port
  [config new-port]
  (update-in config [:gh-api-url] (constantly (format "http://localhost:%d/graphql" new-port))))

(t/deftest start-stop-management-api
  (t/testing "that health endpoint returns 200/OK if GitHub API returns 200/OK"
    (fake/with-routes!
      {"/graphql" {:status 200
                   :headers {}
                   :body "{\"data\": {\"__type\": {\"kind\": \"OBJECT\"}}}"}}
      (let [cfg (fix-gh-api-port (conf/config) port)
            server (#'sut/start-management-api cfg)]
        (try
          (t/is (= 200 (:status (http/get (format "http://localhost:%d/health" (:management-api-port cfg))))))
          (finally (#'sut/stop-management-api server))))))
  (t/testing "that health endpoint returns 503/Server Unavailable if GitHub API returns 500/Internal Server Error"
    (fake/with-routes!
      {"/graphql" {:status 500
                   :headers {}
                   :body ""}}
      (let [cfg (fix-gh-api-port (conf/config) port)
            server (#'sut/start-management-api cfg)]
        (try
          (t/is (= 503 (:status (http/get (format "http://localhost:%d/health" (:management-api-port cfg)) {:throw-exceptions false}))))
          (finally (#'sut/stop-management-api server)))))))
