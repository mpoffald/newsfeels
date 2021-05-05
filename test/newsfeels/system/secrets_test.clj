(ns newsfeels.system.secrets-test
  (:require
   [clojure.test :refer :all]
   [com.stuartsierra.component :as component]
   [newsfeels.system.secrets :as secrets]))

(def secrets {:component1 {:secret1 "secret1"}
              :component2 {:secret2 "secret2"}})

(deftest test-prepare-get-in-secrets
  (let [prepared-secrets (secrets/prepare-secrets secrets)
        secrets-component {:secrets prepared-secrets}]
    (is (= "secret1"
           (secrets/get-in-secrets secrets-component [:component1 :secret1])))
    (is (= "secret2"
           (secrets/get-in-secrets secrets-component [:component2 :secret2])))))


(deftest ^:integration test-secrets-secrecy
  (let [config {:secrets {:secrets-path "test-resources/completely-fake-test-secrets.edn"}} 
        test-system (component/system-map
                     :secrets (secrets/secrets (get config :secrets)))
        started (component/start test-system)
        secrets (:secrets started)]
    (try
      (testing "printing component does not reveal secrets"
        (let [printed-component (pr-str secrets)]
          (is (.contains printed-component "component1"))
          (is (not (.contains printed-component "very-safe-1")))
          (is (not (.contains printed-component "very-safe-2")))))
      (finally (component/stop started)))))
