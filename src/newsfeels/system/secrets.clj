(ns newsfeels.system.secrets
  (:require
   [clojure.edn :as edn]
   [com.stuartsierra.component :as component]
   [taoensso.timbre :as timbre
    :refer [info warn error]]))


;; TODO there must be a better way to do this
(defn prepare-secrets
  "Obscures secrets so they don't get printed in stack traces"
  [secrets]
  (into {}
        (map (fn [[component m]]
               [component (into {}
                                (map (fn [[k s]]
                                       [k (constantly s)])
                                     m))]))
        secrets))

(defn get-in-secrets
  [secrets-component path]
  (let [{:keys [:secrets]} secrets-component
        secret-fn (get-in secrets path)]
    (when (fn? secret-fn)
      (secret-fn))))

(defrecord Secrets []
    component/Lifecycle

    (start [component]
      (info "Starting Secrets")
      (let [{:keys [secrets-path]} component
            secrets (edn/read-string (slurp secrets-path))
            obscured-secrets (prepare-secrets secrets)]
        (assoc component :secrets obscured-secrets)))

    (stop [component]
      (info "Stopping Secrets")
      (assoc component :secrets nil)))

(defn secrets
  [config]
  (map->Secrets config))
