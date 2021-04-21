(ns newsfeels.integrations.nytimes-test
  (:require
   [clojure.test :refer :all]
   [newsfeels.integrations.nytimes :as nytimes]))

(deftest test-build-mostpopular-path
  (is (= "svc/mostpopular/v2/shared/1.json"
         (nytimes/build-mostpopular-path :shared 1)))
  (is (= "svc/mostpopular/v2/viewed/1.json"
         (nytimes/build-mostpopular-path :viewed 1)))
  (is (= "svc/mostpopular/v2/emailed/1.json"
         (nytimes/build-mostpopular-path :emailed 1))))
