(ns github-api-client.app-test
  (:require [github-api-client.app :as sut]
            [clojure.test :as t]))

(t/deftest manifest-map
  (t/testing "that manifest-map loads META-INF/MANIFEST.MF for clojure.main"
    (let [loaded-manifest (#'sut/manifest-map "clojure.main")]
      (t/is (not-empty loaded-manifest))
      (t/is (= "clojure.main" (get loaded-manifest "Main-Class")))))
  (t/testing "that manifest-map returns empty map if supplied class is not in jar"
    (let [loaded-manifest (#'sut/manifest-map "java.lang.String")]
      (t/is (empty loaded-manifest)))))
