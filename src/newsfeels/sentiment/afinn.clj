(ns newsfeels.sentiment.afinn
  "Component that performs cleaning and sentiment analysis
  of text using the AFINN-111 lexicon"
  (:require
   [com.stuartsierra.component :as component]
   [clojure.edn :as edn]
   [clojure.string :as str]
   [taoensso.timbre :as timbre
    :refer [info warn error]]))

(def special-phrases
  "The only multi-word phrases in the AFINN-111 lexicon.
  These need to be kept intact and treated as one word
  in analysis"
  #{"cant stand"
    "cashing in"
    "cool stuff"
    "does not work"
    "fed up"
    "green wash"
    "green washing"
    "messing up"
    "no fun"
    "not good"
    "not working"
    "right direction"
    "screwed up"
    "some kind"
    "dont like"})

(def collapse-characters-re 
  "punctuation marks that should be removed,
  but not be used to split words, eg can't -> cant"
  #"[']")

(def split+trim-characters-re
  "punctuation marks and characters
  that should be removed, and used to split words."
  #"\s+|[%!.,;:*?&$“]")


(defn clean-text
  "Given a string, returns list of individual words
  without capitalization or punctuation.

  Keeps special phrases (multi-word phrases
  that need to be treated as one word) intact."
  [text-str]
  (let [split-word-ls (-> text-str
                          ;; remove leading/trailing whitespace
                          (str/trim)
                          ;; all lower case
                          (str/lower-case)
                          ;; split on whitespace and special characters
                          (str/split split+trim-characters-re))
        cleaned-word-ls (into []
                              (comp
                               ;; get rid of all the empty strings
                               (remove str/blank?)
                               ;; remove the punctuation that isn't for splitting
                               (map
                                (fn [word]
                                  (str/replace word collapse-characters-re ""))))
                              split-word-ls)]
    ;; Look for special two- and three-word phrases to be treated as single words
    (loop [word-ls cleaned-word-ls 
           final-ls []]
      (cond
        (empty? word-ls) final-ls
        
        :else (let [[word1 word2 word3] word-ls
                    possible-two-word-special-phrase (some->> word2
                                                              (str word1 " "))
                    possible-three-word-special-phrase (some->> word3
                                                                (str possible-two-word-special-phrase " "))]
                (cond
                  ;; We assume longer phrases take precedence over shorter ones
                  (contains? special-phrases possible-three-word-special-phrase)
                  (recur (drop 3 word-ls)
                         (conj final-ls possible-three-word-special-phrase))

                  (contains? special-phrases possible-two-word-special-phrase)
                  (recur (drop 2 word-ls)
                         (conj final-ls possible-two-word-special-phrase))

                  :else
                  (recur (rest word-ls) 
                         (conj final-ls word1))))))))

(defn calculate-valence
  "Calculates the valence for a single piece of cleaned text"
  [lexicon cleaned-word-list]
  (apply + (map (fn [word]
                  (or (get lexicon word) 0))
                cleaned-word-list)))

(defn assoc-all-valence-scores
  [afinn articles]
  (let [{:keys [lexicon]} afinn 
        clean-and-calculate (fn [text]
                              (calculate-valence
                               lexicon
                               (clean-text text)))]
    (into []
          (map (fn [article]
                 (let [{:keys [:newsfeels.article/headline
                               :newsfeels.article/abstract]} article
                       headline-score (clean-and-calculate headline)
                       abstract-score (clean-and-calculate abstract)]
                   (-> article
                       (assoc :newsfeels.sentiment.afinn/headline-score headline-score)
                       (assoc :newsfeels.sentiment.afinn/abstract-score abstract-score)))))
          articles)))

(defrecord Afinn []
  component/Lifecycle

  (start [component]
    (info "Starting Afinn")

    (let [{:keys [lexicon-file]} component
          lexicon (edn/read-string (slurp lexicon-file))]
      (assoc component :lexicon lexicon)))

  (stop [component]
    (info "Stopping Afinn")
    (assoc component :lexicon nil)))

(defn afinn
  [config]
  (map->Afinn config))
