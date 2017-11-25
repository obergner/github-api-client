(ns github-api-client.storage-test
  (:require [github-api-client.storage :as sut]
            [clojure.test :as t]))

(t/deftest store-and-get
  (t/testing "that get retrieves what store has put"
    (let [key "store-and-get1"
          value '({:a 1} {:b 2})]
      (sut/store key value)
      (t/is (= value (sut/get key))))))
