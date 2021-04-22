(ns newsfeels.core
  (:require
   [com.stuartsierra.component :as component]
   [newsfeels.integrations.nytimes :as nytimes]))

(defn system
  []
  (let [config (clojure.edn/read-string (slurp "config/config.edn"))]
    (component/system-map
     :nytimes (nytimes/nytimes-client (get config :nytimes)))))

(def current-system (system))

(defn start! []
  (alter-var-root #'current-system component/start)
  nil)

(defn stop! []
  (alter-var-root #'current-system component/stop)
  nil)
