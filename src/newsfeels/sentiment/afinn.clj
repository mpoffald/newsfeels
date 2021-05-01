(ns newsfeels.sentiment.afinn
  (:require
   [com.stuartsierra.component :as component]
   [taoensso.timbre :as timbre
    :refer [info warn error]]))

(defrecord Afinn
    []

    component/Lifecycle

    (start [component]
      (info "Starting Afinn")

      (let [{:keys [lexicon-file]} component
            lexicon (clojure.edn/read-string (slurp lexicon-file))]
        (assoc component :lexicon (atom lexicon))))

    (stop [component]
      (info "Stopping Afinn")
      (assoc component :lexicon nil)))

(defn afinn
  [config]
  (map->Afinn config))
