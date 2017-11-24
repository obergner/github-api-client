(ns github-api-client.storage
  (:require [github-api-client.conf :as conf]
            [clojure.tools.logging :as log])
  (:import (org.mapdb DBMaker)))

(def ^:private db-instance (atom nil))
(def ^:private map-instance (atom nil))

(defn- make-memory-db
  "Create an in-memory, off-heap MapDB instance"
  []
  (-> (DBMaker/memoryDB)
      (.make)))

(defn- db
  "Return this application's singleton MapDB instance,
  optionally creating it if it does not exist"
  []
  (swap! db-instance (fn [d]
                       (if d d (make-memory-db)))))

(defn- map-db
  "Return this application's singleton treeMap DB instance,
  optionally creating it if it does not exist"
  []
  (-> (db)
      (.hashMap "github-events")
      (.createOrOpen)))

(defn store
  "Store 'value' in map db under key 'key'"
  [key value]
  (-> (map-db)
      (.put key value)))
