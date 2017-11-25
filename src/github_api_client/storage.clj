(ns github-api-client.storage
  (:require [clojure.core.async :as async]
            [clojure.tools.logging :as log])
  (:import (org.rocksdb RocksDB Options)))

;; RocksDB layer, the new hotness

(def ^:private dbchan (async/chan 10))

(defn put
  "Store `value` under `key` in backing store.
  Returns `value`.
  This function must not be called from an IOC thread but
  only from a regular thread."
  [key value]
  (async/>!! dbchan [key value])
  value)

(defn- make-rocksdb
  "Create a new `RocksDB` instance using the supplied `config`.
  Return `[db options]`, where `db` is the newly created `RocksDB`
  instance and `options` the `org.rocksdb.Options` instance used
  to create `db`. Return `options` as well since it is backed by
  a C++ object (off-heap) that needs to be closed."
  [config]
  (let [options (-> (Options.)
                    (.setCreateIfMissing true))
        dbpath (:rocksdb-path config ".default.db")
        db (RocksDB/open options dbpath)]
    (log/infof "Opened RocksDB instance [%s] located at [%s] using options [%s]" db dbpath options)
    [db options]))

(defn start-rocksdb
  "Start a go routine that opens a new `RocksDB` instance using the supplied
  `config` to initialise it, then loops waiting for key-value pairs to store.

  If the go routine started by this function receives message `:terminate` on
  `dbchan` it will shut down its `RocksDB` instance and terminate its loop."
  [config]
  (async/go
    (log/infof "Starting RocksDB persistence service")
    (let [db-options (make-rocksdb config)]
      (with-open [db (first db-options)
                  options (second db-options)]
        (loop [msg (async/<! dbchan)]
          (condp = msg
            :terminate (do
                         (log/infof "Received termination message - shutting down RocksDB instance [%s]" db))
            (let [key (first msg)
                  value (second msg)]
              (-> db
                  (.put (.getBytes key) (.getBytes value)))
              (log/debugf "PUT: [%s] -> [%s]" key value)
              (recur (async/<! dbchan)))))))
    (log/infof "RocksDB persistence service stopped")))

(defn stop-rocksdb
  "Stop go routine managing our `RocksDB` instance, closing that instance.
  Returns `true`."
  []
  (async/>!! dbchan :terminate)
  true)
