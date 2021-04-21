(ns newsfeels.integrations.nytimes
  (:require
   [clj-http.client :as client]
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

(defn get-mostpopular-results 
  [client op-map]
  (let [path (build-mostpopular-path op-map)
        response (call-nytimes-api client path)]
    (when (= 200 (:status response))
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
