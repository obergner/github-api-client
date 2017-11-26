(ns github-api-client.github-api-test
  (:require [github-api-client.github-api :as sut]
            [github-api-client.test-http-server :as mock]
            [clojure.test :as t]
            [cheshire.core :as json]
            [clojure.string :as string]))

(t/use-fixtures :once mock/with-mock-server)

(def last-three-prs
  {:status  200
   :headers {}
   :body    (json/generate-string {:data {:organization {:name "tensorflow",
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
                                                                                                     :bodyText "hi i'm back , using only one email."}}]}}}}})})

(t/deftest request-info-client
  (t/testing "that client uses correct URL and API token"
    (let [api-url "http://localhost:3000/graphql"
          api-token "test-token"
          expected (fn [req]
                     (and
                      (= (:uri req) "/graphql")
                      (string/includes? (get-in req [:headers "authorization"] "") api-token)))]
      (mock/when-then expected last-three-prs)
      (let [config {:gh-api-url api-url :gh-api-token api-token}
            test-client (sut/request-info-client config)
            last 3
            resp (test-client "tensorflow" "tensorflow" last)]
        (t/is (= last (count resp)))))))
