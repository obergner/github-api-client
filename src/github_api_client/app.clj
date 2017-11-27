(ns github-api-client.app
  (:require [environ.core :as env]
            [clojure.tools.logging :as log]))

(defn- manifest-map
  "Return the `mainAttributes` of file `META-INF/MANIFEST.MF` located in the jar
  the class name `class-name` is part of, as a map.

  Note that looking up `class-name`'s `MANIFEST.MF` will fail if `clazz` is not stored in a
  jar file. In this case, return an empty map."
  [class-name]
  (try
    (->> (str "jar:"
              (-> (Class/forName class-name)
                  .getProtectionDomain
                  .getCodeSource
                  .getLocation)
              "!/META-INF/MANIFEST.MF")
         clojure.java.io/input-stream
         java.util.jar.Manifest.
         .getMainAttributes
         (map (fn [[k v]] [(str k) v]))
         (into {}))
    (catch Exception e
      (log/warnf e "Failed to load MANIFEST.MF (maybe not running from a jar?): %s" (.getMessage e))
      {})))

(defn log-startup-banner
  "Log a startup banner on behalf of class named `class-name`."
  [class-name]
  (let [manifest (manifest-map class-name)]
    (log/infof "============================================================================")
    (log/infof " Starting: %s v. %s" (get manifest "Application-Name") (get manifest "Application-Version"))
    (log/infof "           %s" (get manifest "Application-Description"))
    (log/infof "")
    (log/infof " Git-Commit: %s" (get manifest "Git-Commit"))
    (log/infof " Git-Branch: %s" (get manifest "Git-Branch"))
    (log/infof " Git-Dirty : %s" (get manifest "Git-Dirty"))
    (log/infof "============================================================================")
    (log/infof "")
    (doseq [[k v] env/env]
      (log/infof "%40.40s: %s" k v))
    (log/infof "")
    (log/infof "============================================================================")))
