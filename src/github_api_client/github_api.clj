(ns github-api-client.github-api
  (:require [clojure.string :as str]
            [cheshire.core :as json]
            [clj-http.client :as http]
            [github-api-client.conf :as conf]
            [clojure.tools.logging :as log]))

(defn- fmt-query
  "Probably format GraphQL query 'query', eliminating line breaks
  since those are not allowed in a well-formed query"
  [query]
  (str/replace query #"\n" ""))

(defn- do-query
  "Issue 'query' to GitHub's GraphQL API, returning the response's body
  as a JSON object"
  ([query]
   (do-query query nil))
  ([query variables]
   (let [fquery (fmt-query query)
         endpoint (:gh-api-url conf/config)
         body (if variables
                (format "{\"query\": \"%s\", \"variables\": %s}" fquery (json/generate-string variables))
                (format "{\"query\": \"%s\"}" fquery))]
     (log/debugf "Issuing GraphQL query [%s] to [%s] ..." body endpoint)
     (let [resp (http/post endpoint
                           {:as :json
                            :content-type :json
                            :accept :json
                            :headers {"authorization" (str "bearer " (:gh-api-token conf/config))}
                            :body body})
           result (:body resp)]
       (log/debugf "[DONE] [%s] -> [%s]" fquery result)
       (:data result)))))

;; Get repository info

(def ^:private q-repository-names
  "query($count: Int = 10) {
    viewer {
     name
     repositories(last: $count) {
       nodes {
         name
       }
     }
   }
  }")

(defn repository-names
  "Get names of all repositories owned by this user"
  [cnt]
  (log/infof "Fetching basic repository info ...")
  (let [repos (get-in (do-query q-repository-names {:count cnt}) [:viewer :repositories :nodes])]
    (log/infof "[DONE] Fetched [%d] repositories" (count repos))
    repos))

;; Get organization info

(def ^:private q-organization-info
  "query($login: String!) {
     organization(login: $login) {
       name
       url 
     }
  }")

(defn organization-info
  "Get basic info on organization identified by login 'login'"
  [login]
  (log/infof "Fetching basic organization info [login: %s] ..." login)
  (let [org (do-query q-organization-info {:login login})]
    (log/infof "[DONE] [%s] -> [%s]" login org)
    org))

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

(defn pull-request-info
  "Get info on last 'last' pull requests in repository 'repo' belonging
  to organization 'login'"
  [login repo last]
  (log/infof "Fetching pull request info [login: %s,repo: %s, last: %d] ..." login repo last)
  (let [resp (do-query q-pull-request-info {:login login :repo repo :last last})
        prs (map #(% :node) (get-in resp [:organization :repository :pullRequests :edges]))]
    (log/infof "[DONE] [%s] -> [%s]" login prs)
    prs))
