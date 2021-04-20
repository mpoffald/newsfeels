(ns newsfeels.core
  (:require
   [com.stuartsierra.component :as component]
   [newsfeels.integrations.nytimes :as nytimes]))

(defn system
  []
  (component/system-map
   :nytimes (nytimes/nytimes-client {})))

(def example-system (component/start (system)))
