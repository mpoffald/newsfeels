(ns newsfeels.utils
  (:require
   [clojure.edn :as edn]))
;; TODO should there be a component for secrets and/or config?

(defn get-config
  []
  (edn/read-string (slurp "config/config.edn")))

;; TODO there must be a better way to do this
(defn prepare-secrets
  "Obscures secrets so they don't get printed in stack traces"
  []
  (let [secrets (edn/read-string (slurp "secrets/secrets.edn"))]
    (into {}
          (map (fn [[k s]]
                 [k  (constantly s)]))
          secrets)))

(defn use-secret
  [secret-fn]
  (secret-fn))
