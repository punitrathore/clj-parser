(ns clj-parser.core-test
  (:require [clj-parser.core :refer :all]
            [clojure.test :refer :all]))


(def cp (parse-char \c))

(deftest test-parsers
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
    (is (= [-123.45, "abc"] (parse-neg-number "-123.45abc")))))



