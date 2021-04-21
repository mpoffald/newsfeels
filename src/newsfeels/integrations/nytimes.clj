(ns newsfeels.integrations.nytimes
  (:require
   [clj-http.client :as client]
   [com.stuartsierra.component :as component]))

(def popularity-types
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
  ([popularity-type period]
   (let [api-path "svc/mostpopular/"
         version-str "v2/"]
     (str api-path version-str (get popularity-types popularity-type) period ".json")))
  ([popularity-type period filter-by]
   (let [api-path "svc/mostpopular/"
         version-str "v2/"]
     (str api-path version-str (get popularity-types popularity-type) period "/" filter-by ".json"))))

(defn get-mostpopular-results 
  ([client popularity-type period]
   (let [path (build-mostpopular-path popularity-type period)
         response (call-nytimes-api client path)]
     (when (= 200 (:status response))
       (get-in response [:body :results]))))
  ([client popularity-type period filter-by]
   (let [path (build-mostpopular-path popularity-type period filter-by)
         response (call-nytimes-api client path)]
     (when (= 200 (:status response))
       (get-in response [:body :results])))))

(defn get-most-emailed
  [client period]
  (get-mostpopular-results client :emailed period))

(defn get-most-shared
  ([client period]     
   (get-mostpopular-results client :shared period))
  ([client period share-type]
   (get-mostpopular-results client :shared period share-type)))

(defn get-most-viewed
  [client period]
  (get-mostpopular-results client :viewed period))

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
