(ns newsfeels.integrations.nytimes
  (:require
   [clj-http.client :as client]
   [clojure.string :as str]
   [com.stuartsierra.component :as component]
   [java-time :as time]))

(def popularity-measures
  {:emailed "emailed/"
   :shared "shared/"
   :viewed "viewed/"})

(defn call-nytimes-api
  [client path]
  (let [{:keys [api-key host]} client
        url (str host path)]
    (client/get url {:as :json
                     :query-params {"api-key" api-key}})))

(defn build-mostpopular-path
  [op-map]
  (let [{:keys [:popularity-measure :period :share-type]} op-map
        api-path "svc/mostpopular/"     ;TODO put in config?
        version-str "v2/"
        share-type-param (some->> share-type
                                  (str "/"))]
    (str api-path
         version-str
         (get popularity-measures popularity-measure)
         period
         share-type-param
         ".json")))

(defn build-article-id
  "Generates a unique id for the article"
  [raw-article-data]
  (let [{:keys [:uri]} raw-article-data
        sha (last (str/split uri #"/"))]
    (str "nytimes-" sha)))

(defn parse-published-date
  "Returns the published-date as a localdate,
  without a time"
  [published-date]
  (let [[y m d] (into []
                      (map clojure.edn/read-string)
                      (str/split published-date #"-"))]
    (time/local-date y m d)))

(defn parse-updated-time
  "Returns the updated-time as an inst,
  assumes UTC"                          ;TODO what *is* the timezone of these timestamps?
  [updated-time]
  (let [[d t]
        (str/split updated-time #" ")]
    (time/instant (str d "T" t "Z"))))

(defn standardize-result
  "Parses a single result from nytimes api, emits a parsed/standardized
  data map comprised of:

   - just fields necessary for downstream consumption/analysis:
     - text fields for analysis (headline, abstract)
     - publication date
     - news source (nytimes)
     - the article's internal id
   - newsfeels.article/raw-article-data:
    map of all the data from the result (minus irrelevant
    image/media data and unused id fields), including nytimes-specific fields
    and various metadata not directly used for text analysis"
  
  ;; FIXME add a field for the time the result was retrieved 
  [raw-article-data]
  (let [raw-article-data (dissoc raw-article-data
                                 :asset_id
                                 :id
                                 :media)
        {:keys [:published_date :title :abstract :updated]} raw-article-data
        article-id (build-article-id raw-article-data)
        raw-article-data-standardized-keys (into {}
                                                 (map (fn [[k v]]
                                                        (let [cleaned (-> k
                                                                          (name)
                                                                          (str/replace "_" "-"))
                                                              new-k (keyword  (str "newsfeels.integrations.nytimes/" cleaned))]
                                                          [new-k v]))
                                                      raw-article-data))
        parsed-published-date (parse-published-date published_date)
        parsed-updated-time (parse-updated-time updated)]
    ;; only the data relevant to downstream text analysis
    {:newsfeels.article/source :nytimes
     :newsfeels.article/id article-id
     :newsfeels.article/headline title
     :newsfeels.article/abstract abstract
     :newsfeels.article/published-date parsed-published-date 
     ;; store everything we know, for safekeeping
     :newsfeels.article/raw-article-data
     (-> raw-article-data-standardized-keys
         (assoc :newsfeels.integrations.nytimes/published-date parsed-published-date)
         (assoc :newsfeels.integrations.nytimes/updated parsed-updated-time)
         (update :newsfeels.integrations.nytimes/adx-keywords #(str/split % #";")))}))


(defn get-mostpopular-results 
  [client op-map]
  (let [path (build-mostpopular-path op-map)
        response (call-nytimes-api client path)]
    (when (= 200 (:status response))  ;TODO log/warn non-200 responses
      (get-in response [:body :results]))))

(defn get-most-emailed
  [client period]
  (get-mostpopular-results client {:popularity-type :emailed
                                   :period period}))

(defn get-most-shared
  [client period & [share-type]]
  (let [base-op-map {:popularity-measure :shared
                     :period period
                     :share-type share-type}
        op-map (into {} (filter (comp some? val) base-op-map))]
    (get-mostpopular-results client op-map)))

(defn get-most-viewed
  [client period]
  (get-mostpopular-results client {:popularity-type :viewed
                                   :period period}))

(defrecord NyTimesClient
    []
    component/Lifecycle

    (start [component]
      (println "Starting NyTimesClient")

      (let [{:keys [api-key]}
            (clojure.edn/read-string (slurp "secrets/secrets.edn"))] ;TODO do this better
        (assoc component :api-key api-key)))

    (stop [component]
      (println "Stopping NyTimesClient")
      (assoc component :api-key nil)))

(defn nytimes-client
  [config]
  (map->NyTimesClient config))
