(ns newsfeels.integrations.nytimes
  (:require
   [clj-http.client :as client]
   [com.stuartsierra.component :as component]))

(def domain "https://api.nytimes.com/")

(defn get-most-popular
  [client period]
  (let [{:keys [api-key]} client]
    (client/get (str domain "svc/mostpopular/v2/emailed/" period ".json")
                {:as :json
                 :query-params {"api-key" api-key}})))

(defrecord NyTimesClient
    []
    component/Lifecycle

    (start [component]
      (println "Starting NyTimesClient")

      (let [{:keys [api-key]}
            (clojure.edn/read-string (slurp "secrets/secrets.edn"))]
        (assoc component :api-key api-key)))

    (stop [component]
      (println "Stopping NyTimesClient")
      (assoc component :api-key nil)))

(defn nytimes-client
  [config]
  (map->NyTimesClient config))
