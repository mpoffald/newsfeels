(ns newsfeels.core
  (:require
   [com.stuartsierra.component :as component]
   [newsfeels.utils :as utils]
   [newsfeels.integrations.nytimes :as nytimes]))

(defn system
  []
  (let [config (utils/get-config)]
    (component/system-map
     :nytimes (nytimes/nytimes-client (get config :nytimes)))))

(def system (system))

(defn start! []
  (alter-var-root #'system component/start)
  nil)

(defn stop! []
  (alter-var-root #'system component/stop)
  nil)
