(ns github-api-client.github-api
  (:require [clojure.string :as str]
            [cheshire.core :as json]
            [clj-http.client :as http]
            [clojure.tools.logging :as log]))

(defn- fmt-query
  "Properly format GraphQL query `query`, eliminating line breaks
  since those are not allowed in a well-formed query."
  [query]
  (str/replace query #"\n" ""))

(defn- do-query
  "Issue `query` to GitHub's GraphQL API, returning the response's body
  as a JSON object. If supplied, use `variables` to parameterize `query`.
  Use `config` hash to look up GitHub API URL and access token."
  ([config query]
   (do-query config query nil))
  ([{:keys [gh-api-url gh-api-token]} query variables]
   (let [fquery (fmt-query query)
         body (if variables
                (format "{\"query\": \"%s\", \"variables\": %s}" fquery (json/generate-string variables))
                (format "{\"query\": \"%s\"}" fquery))]
     (log/debugf "Issuing GraphQL query [%s] to [%s] ..." body gh-api-url)
     (let [resp (http/post gh-api-url
                           {:as :json
                            :content-type :json
                            :accept :json
                            :headers {"authorization" (str "bearer " gh-api-token)}
                            :body body})
           result (:body resp)]
       (log/debugf "[DONE] [%s] -> [%s]" fquery result)
       (:data result)))))

;; Pseudo query to ping GitHub, essentially a health check

(def ^:private q-type-info
  "query($typeName: String!) {
     __type(name: $typeName) {
       kind
     }
  }")

(defn commit-type-client
  "Using the supplied `config` create and return a function that takes no arguments and returns
  type information about GitHub's `Commit` type when called."
  [config]
  (fn []
    (log/infof "Fetching type information about GitHub's commit object ...")
    (let [resp (do-query config q-type-info {:typeName "Commit"})
          type-info (get resp :__type)]
      (log/infof "[DONE] [%s]" type-info)
      type-info)))

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
    (log/infof "Fetching last [%d] pull requests from [%s/%s] ..." last login repo)
    (let [resp (do-query config q-pull-request-info {:login login :repo repo :last last})
          prs (map #(% :node) (get-in resp [:organization :repository :pullRequests :edges]))]
      (log/infof "[DONE] [%s/%s last %d] -> [%d pull requests]" login repo last (count prs))
      prs)))
