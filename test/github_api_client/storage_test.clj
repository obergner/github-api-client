(ns github-api-client.storage-test
  (:require [github-api-client.storage :as sut]
            [clojure.test :as t]
            [clojure.core.async :as async]))

(t/deftest make-rocksdb
  (t/testing "that make-rocksdb returns tuple of RocksDB and Options"
    (let [db-options (#'sut/make-rocksdb {:rocksdb-path "./target/.make-rocksdb-test.db"})]
      (with-open [db (first db-options)
                  options (second db-options)]
        (t/is (instance? org.rocksdb.RocksDB db))
        (t/is (instance? org.rocksdb.Options options))))))

(t/deftest start-stop-rocksdb
  (t/testing "that start-rocksdb returns channel"
    (let [db-chan (#'sut/start-rocksdb {:rocksdb-path "./target/.start-stop-rocksdb-test.db"})]
      (try
        (t/is (instance? clojure.core.async.impl.channels.ManyToManyChannel db-chan))
        (finally
          (#'sut/stop-rocksdb db-chan)
          (async/close! db-chan))))))

(t/deftest put-key-value
  (t/testing "that after start-rocksdb we can put at least 20 key-value pairs"
    (let [db-chan (#'sut/start-rocksdb {:rocksdb-path "./target/.put-key-value-test.db"})]
      (try
        (doseq [kv (range 0 20)]
          (sut/put db-chan (str kv) (str kv)))
        (t/is true)
        (finally
          (#'sut/stop-rocksdb db-chan)
          (async/close! db-chan))))))
