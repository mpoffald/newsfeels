(ns newsfeels.core
  (:require
   [com.stuartsierra.component :as component]
   [newsfeels.utils :as utils]
   [newsfeels.integrations.nytimes :as nytimes]
   [newsfeels.sentiment.afinn :as afinn]))

(defn make-system
  []
  (let [config (utils/get-config)]
    (component/system-map
     :afinn (afinn/afinn (get config :afinn))
     :nytimes (nytimes/nytimes-client (get config :nytimes)))))

(def system (make-system))

(defn start! []
  (alter-var-root #'system component/start)
  nil)

(defn stop! []
  (alter-var-root #'system component/stop)
  nil)
