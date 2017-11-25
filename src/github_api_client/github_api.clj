(ns github-api-client.github-api
  (:require [clojure.string :as str]
            [cheshire.core :as json]
            [clj-http.client :as http]
            [clojure.tools.logging :as log]))

(defn- fmt-query
  "Probably format GraphQL query `query`, eliminating line breaks
  since those are not allowed in a well-formed query"
  [query]
  (str/replace query #"\n" ""))

(defn- do-query
  "Issue `query` to GitHub's GraphQL API, returning the response's body
  as a JSON object"
  ([config query]
   (do-query config query nil))
  ([config query variables]
   (let [fquery (fmt-query query)
         endpoint (:gh-api-url config)
         body (if variables
                (format "{\"query\": \"%s\", \"variables\": %s}" fquery (json/generate-string variables))
                (format "{\"query\": \"%s\"}" fquery))]
     (log/debugf "Issuing GraphQL query [%s] to [%s] ..." body endpoint)
     (let [resp (http/post endpoint
                           {:as :json
                            :content-type :json
                            :accept :json
                            :headers {"authorization" (str "bearer " (:gh-api-token config))}
                            :body body})
           result (:body resp)]
       (log/debugf "[DONE] [%s] -> [%s]" fquery result)
       (:data result)))))

;; Get pull request info

(def ^:private q-pull-request-info
  "query($login: String!, $repo: String!, $last: Int = 10) {
     organization(login: $login) {
       name
       description
       url
       repository(name: $repo) {
         url
         updatedAt
         pullRequests(last: $last) {
           edges {
             node {
               id
               title
               bodyText
             }
           }
         }
       }
     }
  }")

(defn request-info-client
  "Using the supplied `config` create and return a function that takes
  `login`, `repo` and `last` and returns the latest `last` pull requests
  in repository `repo` belonging to the organization identified by `login`."
  [config]
  (fn [login repo last]
    (log/infof "Fetching pull request info [login: %s,repo: %s, last: %d] ..." login repo last)
    (let [resp (do-query config q-pull-request-info {:login login :repo repo :last last})
          prs (map #(% :node) (get-in resp [:organization :repository :pullRequests :edges]))]
      (log/infof "[DONE] [%s] -> [%s]" login prs)
      prs)))
