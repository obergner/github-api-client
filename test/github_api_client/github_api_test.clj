(ns github-api-client.github-api-test
  (:require [github-api-client.github-api :as sut]
            [stub-http.core :as fake]
            [clojure.test :as t]
            [cheshire.core :as json]
            [clojure.string :as string]))

(defn- json-body
  [ring-request]
  (update-in ring-request [:body] #(json/generate-string %)))

(def commit-type-info
  {:status 200
   :headers {}
   :body {:data {:__type "OBJECT"}}})

(t/deftest commit-type-client
  (t/testing "that client uses correct URL and API token"
    (fake/with-routes! [gh-api-token "test-token"]
      {(fn [request]
         (and
          (= (:path request) "/graphql")
          (string/includes? (get-in request [:headers :authorization] "") gh-api-token)))
       (json-body commit-type-info)}
      (let [config {:gh-api-url (format "http://localhost:%s/graphql" port) :gh-api-token gh-api-token}
            test-client (sut/commit-type-client config)
            resp (test-client)]
        (t/is (some? resp)))))
  (t/testing "that client sets accept application/json"
    (fake/with-routes!
      {(fn [request]
         (= "application/json" (get-in request [:headers :accept])))
       (json-body commit-type-info)}
      (let [config {:gh-api-url (format "http://localhost:%d/graphql" port) :gh-api-token "test-token"}
            test-client (sut/commit-type-client config)
            resp (test-client)]
        (t/is (some? resp)))))
  (t/testing "that client returns correct type information"
    (fake/with-routes!
      {"/graphql" (json-body commit-type-info)}
      (let [config {:gh-api-url (format "http://localhost:%d/graphql" port) :gh-api-token "test-token"}
            test-client (sut/commit-type-client config)
            expected-type-info (get-in commit-type-info [:body :data :__type])
            resp (test-client)]
        (t/is (= expected-type-info resp))))))

(def last-three-prs
  {:status  200
   :headers {}
   :body    {:data {:organization {:name "tensorflow",
                                   :description "",
                                   :url "https://github.com/tensorflow",
                                   :repository {:url "https://github.com/tensorflow/tensorflow",
                                                :updatedAt "2017-11-24T18:03:26Z",
                                                :pullRequests {:edges [{:node {:id "MDExOlB1bGxSZXF1ZXN0MTU0NTQ3MDMz",
                                                                               :title "change bazel-mirror to mirror.bazel",
                                                                               :bodyText "this time i added email"}}
                                                                       {:node {:id "MDExOlB1bGxSZXF1ZXN0MTU0NTQ3ODg0",
                                                                               :title "Replace div by safe_div in embedding_lookup_sparse",
                                                                               :bodyText "Fix #14851\nHow to test\n\n add test case\n pass all tests."}}
                                                                       {:node {:id "MDExOlB1bGxSZXF1ZXN0MTU0NTYxMjIz",
                                                                               :title "change bazel-mirror to mirror.bazel",
                                                                               :bodyText "hi i'm back , using only one email."}}]}}}}}})

(t/deftest request-info-client
  (t/testing "that client uses correct URL and API token"
    (fake/with-routes! [gh-api-token "test-token"]
      {(fn [request]
         (and
          (= (:path request) "/graphql")
          (string/includes? (get-in request [:headers :authorization] "") gh-api-token)))
       (json-body last-three-prs)}
      (let [config {:gh-api-url (format "http://localhost:%s/graphql" port) :gh-api-token gh-api-token}
            last 3
            test-client (sut/request-info-client config)
            resp (test-client "tensorflow" "tensorflow" last)]
        (t/is (= last (count resp))))))
  (t/testing "that client sets accept application/json"
    (fake/with-routes!
      {(fn [request]
         (= "application/json" (get-in request [:headers :accept])))
       (json-body last-three-prs)}
      (let [config {:gh-api-url (format "http://localhost:%d/graphql" port) :gh-api-token "test-token"}
            test-client (sut/request-info-client config)
            last 3
            resp (test-client "tensorflow" "tensorflow" last)]
        (t/is (= last (count resp))))))
  (t/testing "that client returns correctly transformed list of pull requests"
    (fake/with-routes!
      {"/graphql" (json-body last-three-prs)}
      (let [config {:gh-api-url (format "http://localhost:%d/graphql" port) :gh-api-token "test-token"}
            test-client (sut/request-info-client config)
            expected-prs (map #(:node %) (get-in last-three-prs [:body :data :organization :repository :pullRequests :edges]))
            resp (test-client "tensorflow" "tensorflow" 3)]
        (t/is (= expected-prs resp))))))
