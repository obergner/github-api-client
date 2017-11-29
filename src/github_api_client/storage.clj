(ns github-api-client.storage
  (:require [clojure.core.async :as async]
            [mount.core :as mount]
            [github-api-client.conf :as conf]
            [clojure.tools.logging :as log])
  (:import (org.rocksdb RocksDB Options)))

(defn- make-rocksdb
  "Create a new `RocksDB` instance using the supplied `config`.
  Return `[db options]`, where `db` is the newly created `RocksDB`
  instance and `options` the `org.rocksdb.Options` instance used
  to create `db`. Return `options` as well since it is backed by
  a C++ object (off-heap) that needs to be closed."
  [{:keys [rocksdb-path]}]
  (let [options (-> (Options.)
                    (.setCreateIfMissing true))
        rocksdb (RocksDB/open options rocksdb-path)]
    (log/infof "Opened RocksDB instance [%s] located at [%s] using options [%s]" rocksdb rocksdb-path options)
    [rocksdb options]))

(defn- start-rocksdb
  "Start a go routine that opens a new `RocksDB` instance using the supplied
  `config` to initialise it, then loops waiting for key-value pairs to store.

  If the go routine started by this function receives message `:terminate` on
  `dbchan` it will shut down its `RocksDB` instance and terminate its loop."
  [config]
  (let [db-chan (async/chan 10)]
    (async/go
      (log/infof "Starting RocksDB persistence service")
      (let [db-options (make-rocksdb config)]
        (with-open [rocksdb (first db-options)
                    options (second db-options)]
          (loop [msg (async/<! db-chan)]
            (condp = msg
              :terminate (do
                           (log/infof "Received termination message - shutting down RocksDB instance [%s]" rocksdb))
              (let [[key value] msg]
                (-> rocksdb
                    (.put (.getBytes key) (.getBytes value)))
                (log/debugf "PUT: [%s] -> [%s]" key value)
                (recur (async/<! db-chan))))))
        (log/infof "RocksDB persistence service stopped")))
    db-chan))

(defn- stop-rocksdb
  "Stop go routine managing our `RocksDB` instance, closing that instance.
  Returns `true`."
  [db-chan]
  (async/>!! db-chan :terminate)
  true)

(mount/defstate db
  :start (start-rocksdb conf/conf)
  :stop (stop-rocksdb db))

(defn put
  "Store `value` under `key` in backing store.
  Returns `value`.
  This function must not be called from an IOC thread but
  only from a regular thread."
  [db key value]
  (async/>!! db [key value])
  value)
