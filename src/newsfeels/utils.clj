(ns newsfeels.utils
  (:require
   [clojure.edn :as edn]))
;; TODO put this in a component

(defn get-config
  []
  (edn/read-string (slurp "config/config.edn")))
