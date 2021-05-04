(ns newsfeels.core 
  (:require
   [com.stuartsierra.component :as component]
   [clojure.pprint :as pprint]
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
          scored-articles (afinn/assoc-all-valence-scores afinn most-viewed-articles)]
      (println "Most-Viewed Articles in the Last 24 Hours:")
      (pprint/print-table ["Source" "Headline" "Headline Valence" "Abstract Valence" "Total Valence"]
                          (sort-by #(get % "Total Valence")
                                   (map (comp
                                         (fn [article] (assoc article "Total Valence" (+ (get article "Headline Valence")
                                                                                         (get article "Abstract Valence"))))
                                         (fn [article] (update article "Headline" #(if (<= (count %) 80)
                                                                                     %
                                                                                     (str (subs % 0 77) "..."))))
                                         (fn [article] (clojure.set/rename-keys article 
                                                                                {:newsfeels.article/source "Source"
                                                                                 :newsfeels.article/headline "Headline"
                                                                                 :newsfeels.sentiment.afinn/headline-score "Headline Valence"
                                                                                 :newsfeels.sentiment.afinn/abstract-score "Abstract Valence"})))
                                        scored-articles))))
    (finally (stop!))))

(defn -main
  [& args]
  (demo)
  (System/exit 0))
