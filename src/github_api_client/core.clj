(ns github-api-client.core
  (:require [environ.core :as env]
            [github-api-client.task :as task]
            [github-api-client.storage :as storage]
            [github-api-client.conf :as conf]
            [clojure.tools.logging :as log])
  (:gen-class))

(defn- manifest-map
  "Returns the mainAttributes of this namespace's MANIFEST.MF as a map.
  Note that looking up a namespace's MANIFEST.MF will fail in many
  situations, e.g. when this application is not running from a jar
  file. In this case this function returns an empty map."
  []
  (try
    (->> (clojure.java.io/resource "META-INF/MANIFEST.MF")
         clojure.java.io/input-stream
         java.util.jar.Manifest.
         .getMainAttributes
         (map (fn [[k v]] [(str k) v]))
         (into {}))
    (catch Exception e
      (log/warnf e "Failed to load MANIFEST.MF (maybe not running from a jar?): %s" (.getMessage e)))
    (finally {})))

(defn- log-startup-banner
  []
  (let [manifest (manifest-map)]
    (log/infof "============================================================================")
    (log/infof " Starting: %s v. %s" (get-in manifest ["Application-Name"]) (get-in manifest ["Application-Version"]))
    (log/infof "           %s" (get-in manifest ["Application-Description"]))
    (log/infof "")
    (log/infof " Git-Commit: %s" (get-in manifest ["Git-Commit"]))
    (log/infof " Git-Branch: %s" (get-in manifest ["Git-Branch"]))
    (log/infof " Git-Dirty : %s" (get-in manifest ["Git-Dirty"]))
    (log/infof "============================================================================")
    (log/infof "")
    (doseq [entry env/env]
      (log/infof "%40.40s: %s" (key entry) (val entry)))
    (log/infof "")
    (log/infof "============================================================================")))

(defn -main
  "Start application"
  [& args]
  (log-startup-banner)
  (storage/start-rocksdb conf/config)
  (task/schedule-event-log conf/config))
