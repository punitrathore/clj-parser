(ns clj-parser.core-test
  (:require [clj-parser.core :refer :all]
            [clojure.test :refer :all]))

(deftest test-compose-parsers
  (let [parser (<=> (parse-char \p)
                    (parse-char \u))]
    (is (= ["pu" "nit"] (parser "punit")))
    (is (= nil (parser "sound")))))

(deftest test-compose-or
  (let [parser (<|> (parse-char \p) (parse-char \s))]
    (is (= ["p" "unit"] (parser "punit")))
    (is (= ["s" "ound"] (parser "sound")))
    (is (= nil (parser "aloha")))))

(deftest test-compose-left
  (let [parser (<* (parse-char \a) (parse-char \ ))]
    (is (= ["a" "smile"] (parser "a smile")))
    (is (= nil (parser "delight")))))

(deftest test-compose-right
  (let [parser (*> (parse-char \ ) (parse-char \a))]
    (is (= ["a" "woke"] (parser " awoke")))
    (is (= nil (parser "sleep")))))

(deftest test-compose-apply
  (let [parser (<f> parse-pos-int #(+ 2 %))]
    (is (= [22 "abc"] (parser "20abc")))
    (is (= nil (parser "abc")))))

(deftest test-parsers
  (let [cp (parse-char \c)]
    (testing "zero-or-more"
      (is (= ["cc" "har"] ((zero-or-more cp) "cchar")))
      (is (= ["" "har"] ((zero-or-more cp) "har"))))
    (testing "one-or-more"
      (is (= ["cc" "har"] ((one-or-more cp) "cchar")))
      (is (= nil ((one-or-more cp) "har"))))
    (testing "pos-int"
      (is (= [123, "abc"] (parse-pos-int "123abc")))
      (is (= nil (parse-pos-int "-123abc"))))
    (testing "neg-int"
      (is (= nil (neg-int "123abc")))
      (is (= [-123, "abc"] (parse-neg-int "-123abc"))))
    (testing "pos-double"
      (is (= [123.45, "abc"] (parse-pos-double "123.45abc")))
      (is (= nil (parse-pos-double "-123.23abc"))))
    (testing "neg-double"
      (is (= nil (parse-neg-double "123.23abc")))
      (is (= [-123.45, "abc"] (parse-neg-double "-123.45abc"))))
    (testing "pos-number"
      (is (= [123.45, "abc"] (parse-pos-number "123.45abc")))
      (is (= nil (parse-pos-number "-123.23abc"))))
    (testing "neg-number"
      (is (= nil (parse-neg-number "123.23abc")))
      (is (= [-123.45, "abc"] (parse-neg-number "-123.45abc"))))))

