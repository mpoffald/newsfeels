(ns newsfeels.sentiment.afinn-test
  (:require
   [clojure.test :refer :all]
   [newsfeels.sentiment.afinn :as afinn]))

(deftest test-clean-text
  (testing "removes all whitespace"
    (let [whitespaced "a b   c\td\ne  f "]
      (is (= ["a" "b" "c" "d" "e" "f"]
             (afinn/clean-text whitespaced)))))
  (testing "removes and splits on punctuation"
    (let [punctuated "a..b,; c! d? e&* $f:g"]
      (is (= ["a" "b" "c" "d" "e" "f" "g"]
             (afinn/clean-text punctuated)))))
  (testing "removes apostrophes and hyphens, leaving word intact"
    (let [special-punctuation "can't it's win-win"]
      (is (= ["cant" "its" "winwin"]
             (afinn/clean-text special-punctuation)))))
  (testing "standardizes to lower-case"
    (let [mixed-case "Aa Bb cC"]
      (is (= ["aa" "bb" "cc"]
             (afinn/clean-text mixed-case)))))
  (testing "keeps special phrases intact"
    (let [special "I can't stand data preprocessing. I don't like all the edge cases"
          special-odd "Sometimes you have to do things you don't like"
          special-even "Blah Blah don't like! Can't stand!"]
      (is (= ["i" "cant stand" "data" "preprocessing"
              "i" "dont like" "all" "the" "edge" "cases"]
             (afinn/clean-text special)))
      (is (= ["sometimes" "you" "have" "to" "do" "things"
              "you" "dont like"]
             (afinn/clean-text special-odd)))
      (is (= ["blah" "blah" "dont like" "cant stand"]
             (afinn/clean-text special-even)))))
  (testing "no false positives for special phrases"
    (let [not-special "I guess I don't mind so much. I can't be too mad!"]
      (is (= ["i" "guess" "i" "dont" "mind" "so" "much"
              "i" "cant" "be" "too" "mad"]
             (afinn/clean-text not-special))))))