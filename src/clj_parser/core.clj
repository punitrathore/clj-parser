(ns clj-parser.core
  (:require [clojure.test :refer :all]))

;; A parser is a function which accepts a string.
;; When the parsing is successful, the function returns a vector with two elements. The first element is the parsed value, and the second elemnt is the remaining string.
;;
;; On unsuccessful parsing, the return value is `nil`
;; Then type definition would look like this -
;; parser-fn :: String -> [<parsed-value>, <remaining-string>]

(defn satisfy [f]
  (fn [s]
    (let [fch (first s)]
      (if (and fch (f fch))
        [(str fch) (apply str (rest s))]))))

(defn parse-char [ch]
  (satisfy #(= % ch)))

(defn parse-word [word]
  (reduce (fn [parser ch]
            (<=> parser (parse-char ch)))
          identity-parser word))

(defn compose-parsers [p1 p2]
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

(def <=> compose-parsers)
(def <* compose-left)
(def *> compose-right)
(def <|> compose-or)
(def <f> compose-apply)

(def identity-parser
  (fn [s]
    ["" s]))

(defn optional-value [val]
  (fn [s]
    [val s]))

(defn optional-parser
  ([parser]
   (optional-parser parser ""))
  ([parser default-value]
   (fn [s]
     (if-let [result (parser s)]
       result
       [default-value s]))))

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

(def alphanumeric
  (satisfy #(Character/isLetterOrDigit %)))

(def word
  (<* (one-or-more alphanumeric)
      (optional-parser (parse-char \ ))))

(def spaces (zero-or-more (parse-char \ )))

(defn safe-to-double [s]
  (if s
    (Double/parseDouble s)))

(defn safe-to-int [s]
  (if s
    (Integer/parseInt s)))

(def pos-int (one-or-more digit))

(def neg-int
  (-> (parse-char \-)
      (<=> pos-int)))

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

(def parse-pos-int
  (<f> pos-int safe-to-int))

(def parse-neg-int
  (<f> neg-int safe-to-int))

(def parse-any-int
  (<f> any-int safe-to-int))

(def parse-pos-double
  (<f> pos-double safe-to-double))

(def parse-neg-double
  (<f> neg-double safe-to-double))

(def parse-pos-double
  (<f> pos-double safe-to-double))

(def parse-pos-number
  (<f> pos-number safe-to-double))

(def parse-neg-number
  (<f> neg-number safe-to-double))

(def parse-any-number
  (<f> any-number safe-to-double))
