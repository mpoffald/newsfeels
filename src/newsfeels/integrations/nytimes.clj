(ns newsfeels.integrations.nytimes
  (:require
   [clj-http.client :as client]
   [com.stuartsierra.component :as component]))

(def popularity-types
  {:emailed "emailed/"
   :shared "shared/"
   :viewed "viewed/"})

(defn call-nytimes-mostpopular
  [client popularity-type period & opts]
  (let [api-path "svc/mostpopular/"
        version-str "v2/"
        {:keys [api-key host]} client]
    (client/get
     (str host api-path version-str (get popularity-types popularity-type) period ".json")
     {:as :json
      :query-params {"api-key" api-key}})))

(defn get-most-emailed
  [client period]
  (call-nytimes-mostpopular client :emailed period))

(defn get-most-shared
  [client period]                       ;TODO support optional shared-method
  (call-nytimes-mostpopular client :shared period))

(defn get-most-viewed
  [client period]
  (call-nytimes-mostpopular client :viewed period))

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
