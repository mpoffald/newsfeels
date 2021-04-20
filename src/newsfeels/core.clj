(ns newsfeels.core
  (:require
   [com.stuartsierra.component :as component]
   [newsfeels.integrations.nytimes :as nytimes]))

(defn system
  []
  (let [config (clojure.edn/read-string (slurp "config/config.edn"))]
    (component/system-map
     :nytimes (nytimes/nytimes-client (get config :nytimes)))))

(def example-system (component/start (system)))
