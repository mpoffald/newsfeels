(ns newsfeels.core 
  (:require
   [com.stuartsierra.component :as component]
   [clojure.pprint :as pprint]
   [clojure.set :as set]
   [newsfeels.utils :as utils]
   [newsfeels.integrations.nytimes :as nytimes]
   [newsfeels.sentiment.afinn :as afinn])
  (:gen-class))

(defn make-system
  []
  (let [config (utils/get-config)]
    (component/system-map
     :afinn (afinn/afinn (get config :afinn))
     :nytimes (nytimes/nytimes-client (get config :nytimes)))))

(def system (make-system))

(defn start! []
  (alter-var-root #'system component/start)
  nil)

(defn stop! []
  (alter-var-root #'system component/stop)
  nil)

(defn demo
  "Retrieves most-viewed articles from the last day
  and prints a table of valence values"
  []
  (start!)
  (try
    (let [{:keys [afinn nytimes]} system
          most-viewed-articles (nytimes/get-most-viewed nytimes 1)
          scored-articles (afinn/assoc-all-valence-scores afinn most-viewed-articles)
          source-str "Source"
          headline-str "Headline"
          headline-valence-str "Headline Valence"
          abstract-valence-str "Abstract Valence"
          total-valence-str "Total Valence"]
      (println "Most-Viewed Articles in the Last 24 Hours:")
      (pprint/print-table [source-str headline-str headline-valence-str abstract-valence-str total-valence-str]
                          (sort-by #(get % total-valence-str)
                                   (map (comp
                                         (fn [article] (assoc article total-valence-str (+ (get article headline-valence-str)
                                                                                           (get article abstract-valence-str))))
                                         (fn [article] (update article headline-str #(if (<= (count %) 80)
                                                                                       %
                                                                                       (str (subs % 0 77) "..."))))
                                         (fn [article] (set/rename-keys article 
                                                                        {:newsfeels.article/source source-str
                                                                         :newsfeels.article/headline headline-str
                                                                         :newsfeels.sentiment.afinn/headline-score headline-valence-str
                                                                         :newsfeels.sentiment.afinn/abstract-score abstract-valence-str})))
                                        scored-articles))))
    (finally (stop!))))

(defn -main
  [& args]
  (demo)
  (System/exit 0))
