(ns github-api-client.app-test
  (:require [github-api-client.app :as sut]
            [clojure.test :as t]))

(t/deftest manifest-map
  (t/testing "that manifest-map loads META-INF/MANIFEST.MF from classpath"
    ;; Not that this will load SOME, non-deterministically found MANIFEST.MF
    (let [loaded-manifest (#'sut/manifest-map)]
      (t/is (not-empty loaded-manifest)))))
