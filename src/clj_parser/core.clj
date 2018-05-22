(ns clj-parser.core
  (:require [clojure.test :refer :all]))

;; Parser :: String -> [[<parsed-string>], <remaining-string>]

(def extract-value first)

(defn satisfy [f]
  (fn [s]
    (let [fch (first s)]
      (if (and fch (f fch))
        [(str fch) (apply str (rest s))]))))

(defn parse-char [ch]
  (satisfy #(= % ch)))

(defn compose-parser [p1 p2]
  (fn [s]
    (if-let [[p1f rs1 :as p1-parsed] (p1 s)]
      (if-let [[p2f rs2 :as p2-parsed] (p2 rs1)]
        (cond (and (string? p1f) (string? p2f))
              [(str p1f p2f) rs2]

              (vector? p1f)
              [(vec (conj p1f p2f)) rs2]

              :else
              [[p1f p2f] rs2])))))

(defn compose-left [p1 p2]
  (fn [s]
    (if-let [[p1f rs1 :as p1-parsed] (p1 s)]
      (if-let [[p2f rs2 :as p2-parsed] (p2 rs1)]
        [p1f rs2]))))

(defn compose-right [p1 p2]
  (fn [s]
    (if-let [[p1f rs1 :as p1-parsed] (p1 s)]
      (if-let [[p2f rs2 :as p2-parsed] (p2 rs1)]
        [p2f rs2]))))

(defn compose-or [p1 p2]
  (fn [s]
    (if-let [[p-str, rem-str :as p1-parsed] (p1 s)]
      p1-parsed
      (p2 s))))

(defn compose-apply [p f]
  (fn [s]
    (if-let [[p-str rem-str] (p s)]
      [(f p-str) rem-str])))

(def <=> compose-parser)
(def <* compose-left)
(def *> compose-right)
(def <|> compose-or)
(def <f> compose-apply)

(def identity-parser
  (fn [s]
    ["" s]))

(defn optional-parser [val]
  (fn [s]
    [val s]))

(declare one-or-more)

(defn zero-or-more [p]
  (<|> (one-or-more p)
       identity-parser))

(defn one-or-more [p]
  (fn [s]
    (let [[pf rs :as p-parsed] (p s)]
      (if p-parsed
        (if-let [[pf' rs' :as p-parsed'] ((zero-or-more p) rs)]
          [(str pf pf') rs']
          p-parsed)))))

(def digit
  (satisfy #(Character/isDigit %)))

(def spaces (zero-or-more (parse-char \ )))

(defn fmap [f [parsed-result remaining-str :as result]]
  (if result
        [(f parsed-result) remaining-str]))

(defn safe-to-double [s]
  (if s
    (Double/parseDouble s)))

(defn safe-to-int [s]
  (if s
    (Integer/parseInt s)))

(def pos-int (one-or-more digit))

(def neg-int
  (-> (parse-char \-)
      (compose-parser pos-int)))

(def any-int
  (<|> neg-int pos-int))

(def pos-double
  (-> pos-int
      (<=> (parse-char \.))
      (<=> pos-int)))

(def neg-double
  (-> (parse-char \-)
      (<=> pos-double)))

(def pos-double
  (-> pos-int
      (<=> (parse-char \.))
      (<=> pos-int)))

(def any-double
  (<|> neg-double pos-double))

(def pos-number
  (<|> pos-double pos-int))

(def neg-number
  (<|> neg-double neg-int))

(def any-number
  (<|> neg-number pos-number))

(defn make-parser [s format-fn parser]
  (fmap format-fn (parser s)))

(defn parse-pos-int [s]
  (make-parser s safe-to-int pos-int))

(defn parse-neg-int [s]
  (make-parser s safe-to-int neg-int))

(defn parse-any-int [s]
  (make-parser s safe-to-int any-int))

(defn parse-pos-double [s]
  (make-parser s safe-to-double pos-double))

(defn parse-neg-double [s]
  (make-parser s safe-to-double neg-double))

(defn parse-pos-double [s]
  (make-parser s safe-to-double pos-double))

(defn parse-pos-number [s]
  (make-parser s safe-to-double pos-number))

(defn parse-neg-number [s]
  (make-parser s safe-to-double neg-number))

(defn parse-any-number [s]
  (make-parser s safe-to-double any-number))

(defn parse-any-number [s]
  (make-parser s safe-to-double any-number))

