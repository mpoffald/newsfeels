(start!)

(require '[newsfeels.integrations.nytimes :as nyt])

(def client (:nytimes system))

(def most-viewed (nyt/get-most-viewed client 1))

(require '[newsfeels.sentiment.afinn :as afinn])

(def afinn (:afinn system))

(def scored (afinn/assoc-all-valence-scores afinn most-viewed))

(def interesting-bits (into [] (map #(select-keys % [:newsfeels.sentiment.afinn/headline-score
                                                     :newsfeels.sentiment.afinn/abstract-score
                                                     :newsfeels.article/headline
                                                     :newsfeels.article/abstract])) scored))

(stop!)
