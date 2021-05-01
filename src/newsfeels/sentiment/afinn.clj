(ns newsfeels.sentiment.afinn
  "Component that performs cleaning and sentiment analysis
  of text using the AFINN-111 lexicon"
  (:require
   [com.stuartsierra.component :as component]
   [clojure.string :as str]
   [taoensso.timbre :as timbre
    :refer [info warn error]]))

(def special-phrases
  "The only multi-word phrases in the AFINN-111 lexicon.
  These need to be kept intact and treated as one word
  in analysis  "
  #{"cant stand"
    "dont like"})

(def special-punctuation-to-remove
  "punctuation marks that should be removed,
  but not be used to split words, eg can't -> cant"
  "[-']")

(def special-characters-split-trim-re
  "punctuation marks and special characters
  that should be removed, and used to split words."
  "[!.,;:*?&$]")


(defn clean-text
  "Given a string, returns list of individual words
  without capitalization or punctuation"
  [text-str]
  (let [split-word-ls (-> text-str
                          ;; remove leading/trailing whitespace
                          (str/trim)
                          ;; all lower case
                          (str/lower-case)
                          ;; split on whitespace and special characters
                          (str/split (re-pattern
                                      (str "\\s+"
                                           "|"
                                           special-characters-split-trim-re))))
        cleaned-word-ls (into []
                              (comp
                               ;; get rid of all the empty strings
                               (remove str/blank?)
                               ;; remove the punctuation that isn't for splitting
                               (map
                                (fn [word]
                                  (str/replace
                                   word
                                   (re-pattern special-punctuation-to-remove)
                                   ""))))
                              split-word-ls)]
    ;; Look for special two-word phrases to be treated as single words
    (loop [word-ls (rest cleaned-word-ls) 
           word1 (first cleaned-word-ls)
           final-ls []]
      (cond
        (empty? word-ls) final-ls
       :else 
        (let [word2 (first word-ls)
              possible-special-phrase (str word1 " " word2)]
          (if (contains? special-phrases possible-special-phrase)
            (recur (drop 2 word-ls)
                   (second word-ls)
                   (conj final-ls possible-special-phrase))
            (recur (rest word-ls) 
                   (first word-ls)
                   (if (= 1 (count word-ls))
                     (conj final-ls word1 word2)
                     (conj final-ls word1)))))))))

(defrecord Afinn
    []

    component/Lifecycle

    (start [component]
      (info "Starting Afinn")

      (let [{:keys [lexicon-file]} component
            lexicon (clojure.edn/read-string (slurp lexicon-file))]
        (assoc component :lexicon (atom lexicon))))

    (stop [component]
      (info "Stopping Afinn")
      (assoc component :lexicon nil)))

(defn afinn
  [config]
  (map->Afinn config))
