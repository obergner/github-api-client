(ns github-api-client.conf-test
  (:require [github-api-client.conf :as sut]
            [clojure.test :as t]
            [environ.core :as env]))

(t/deftest config
  (t/testing "that config correctly reads configuration from environment variables"
    (let [exp-log-interval-ms (read-string (env/env :log-interval-ms))
          exp-gh-api-url (env/env :gh-api-url)
          exp-gh-api-token (env/env :gh-api-token)
          exp-gh-org (env/env :gh-org)
          exp-gh-repo (env/env :gh-repo)
          exp-gh-prs-last (read-string (env/env :gh-prs-last))
          exp-rocksdb-path (env/env :rocksdb-path)
          exp-management-api-port (read-string (env/env :management-api-port))
          actual-config (sut/config)]
      (t/is (= exp-log-interval-ms (:log-interval-ms actual-config)))
      (t/is (= exp-gh-api-url (:gh-api-url actual-config)))
      (t/is (= exp-gh-api-token (:gh-api-token actual-config)))
      (t/is (= exp-gh-org (:gh-org actual-config)))
      (t/is (= exp-gh-repo (:gh-repo actual-config)))
      (t/is (= exp-gh-prs-last (:gh-prs-last actual-config)))
      (t/is (= exp-rocksdb-path (:rocksdb-path actual-config)))
      (t/is (= exp-management-api-port (:management-api-port actual-config))))))
