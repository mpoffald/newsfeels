(ns newsfeels.integrations.nytimes-test
  (:require
   [clojure.test :refer :all]
   [newsfeels.integrations.nytimes :as nytimes]))

(deftest test-build-mostpopular-path
  (is (= "svc/mostpopular/v2/shared/1.json"
         (nytimes/build-mostpopular-path {:popularity-measure  :shared
                                          :period 1})))
  (is (= "svc/mostpopular/v2/shared/1/facebook.json"
         (nytimes/build-mostpopular-path {:popularity-measure :shared
                                          :period 1
                                          :share-type "facebook"})))
  (is (= "svc/mostpopular/v2/viewed/1.json"
         (nytimes/build-mostpopular-path {:popularity-measure :viewed
                                          :period  1})))
  (is (= "svc/mostpopular/v2/emailed/1.json"
         (nytimes/build-mostpopular-path {:popularity-measure :emailed
                                          :period 1}))))

;; example of a single return value from the api
(def complete-example-result
  {:org_facet [],
   :geo_facet [],
   :published_date "2021-04-19",
   :updated "2021-04-21 17:43:28",
   :section "Well",
   :abstract "Brief one or two sentence description of article contents.",
   :per_facet [],
   :type "Article",
   :source "New York Times",
   :nytdsection "well",
   :title "Article Headline" ,
   :eta_id 0,
   :column nil,
   :asset_id 100000007694130,
   :byline "By Example Author",
   :id 100000007694130,
   :des_facet ["Mental Health and Disorders"
               "Anxiety and Stress"
               "Loneliness"
               "Depression (Mental)"
               "Grief (Emotion)"
               "Psychology and Psychologists"
               "Coronavirus (2019-nCoV)"
               "Quarantine (Life and Culture)"
               "Content Type: Service"],
   :url "https://www.nytimes.com/2021/04/19/section/subsection/example-article.html",
   :adx_keywords "Example Keyword1;Example Keyword2;Example Keyword3",
   :uri "nyt://article/11fb43c2-a754-5f02-aecf-cb7244acb75d",
   :media [{:type "image",
            :subtype "photo",
            :caption "",
            :copyright "Copyright Name",
            :approved_for_syndication 1,
            :media-metadata
            [{:url "https://static01.nyt.com/images/example/path/example-image1.png",
              :format "Standard Thumbnail",
              :height 75,
              :width 75}
             {:url "https://static01.nyt.com/images/example/path/example-image2.png",
              :format "mediumThreeByTwo210",
              :height 140,
              :width 210}
             {:url "https://static01.nyt.com/images/example/path/example-image3.png",
              :format "mediumThreeByTwo440",
              :height 293,
              :width 440}]}],
   :subsection "Mind"})

(def correct-standardized-id "nytimes-11fb43c2-a754-5f02-aecf-cb7244acb75d")

(deftest test-build-article-id
  (is (= correct-standardized-id
         (nytimes/build-article-id complete-example-result))))

(deftest test-standardized-result
  (let [standardized (nytimes/standardize-result complete-example-result)]
    (is (= :nytimes
           (get standardized :newsfeels.article/source)))
    (is (= correct-standardized-id
           (get standardized :newsfeels.article/id)))
    (testing "use title as headline"
      (is (= (:title complete-example-result) 
             (get standardized :newsfeels.article/headline))))
    (is (= (:abstract complete-example-result) 
           (get standardized :newsfeels.article/abstract)))
    (is (= (:published_date complete-example-result) ;FIXME should be a parsed date, not a string
           (get standardized :newsfeels.article/published-date)))
    (testing "correctly parses list of adx keywords"
      (is (= ["Example Keyword1"
              "Example Keyword2"
              "Example Keyword3"]
             (get standardized :newsfeels.integrations.nytimes/adx-keywords))))))
