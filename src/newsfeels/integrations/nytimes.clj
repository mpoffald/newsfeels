(ns newsfeels.integrations.nytimes
  (:require
   [clj-http.client :as client]
   [clojure.string :as str]
   [com.stuartsierra.component :as component]))

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
  [raw-article-data]
  (let [{:keys [:uri]} raw-article-data
        sha (last (str/split uri #"/"))]
    (str "nytimes-" sha)))

(defn standardize-result
  ;; FIXME this definitely needs a docstring
  ;; FIXME add a field for the time the result was retrieved 
  [raw-article-data]
  (let [raw-article-data (dissoc raw-article-data
                                 :asset_id
                                 :id
                                 :media)
        {:keys [:published_date :title :abstract :updated]} raw-article-data
        article-id (build-article-id raw-article-data)
        article-data-standardized-keys (into {}
                                             (map (fn [[k v]]
                                                    (let [cleaned (-> k
                                                                      (name)
                                                                      (str/replace "_" "-"))
                                                          new-k (keyword  (str "newsfeels.integrations.nytimes/" cleaned))]
                                                      [new-k v]))
                                                  raw-article-data))]
    (-> article-data-standardized-keys
        (assoc :newsfeels.article/source :nytimes)
        (assoc :newsfeels.article/id article-id)
        (assoc :newsfeels.article/headline title)
        (assoc :newsfeels.article/abstract abstract)
        (update :newsfeels.integrations.nytimes/adx-keywords #(str/split % #";"))
        ;; FIXME parse into actual times instead
        (assoc :newsfeels.article/published-date published_date)
        (assoc :newsfeels.article/updated-time updated))))

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
  (get-mostpopular-results client {:popuarity-type :viewed
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
