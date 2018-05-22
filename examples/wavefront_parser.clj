(ns wavefront-parser
  (:require [clj-parser.core :refer :all])
  (:import [clojure.java.io]))

(defrecord Vertex [x y z w])
(defrecord TextureCoord [u v w])
(defrecord Normal [x y z])
(defrecord FaceElement [vertex texture-coord normal])
(defrecord Face [elements])


(defn new-vertex [[x y z w]]
  (Vertex. x y z w))

(defn new-tc [[u v w]]
  (TextureCoord. u v w))

(defn new-normal [[x y z]]
  (Normal. x y z))

(defn new-face-element [[v tc n]]
  (FaceElement. v tc n))

(defn new-face [elements]
  (Face. elements))

(def parse-vertex
  (-> parse-any-number
      (<* spaces)
      (<=> parse-any-number)
      (<* spaces)
      (<=> parse-any-number)
      (<* spaces)
      (<=> (<|> parse-any-number (optional-parser 1.0)))
      (<f> new-vertex)))

(def parse-vline
  (-> (parse-char \v)
      (*> spaces)
      (*> parse-vertex)))

(def parse-tc
  (-> parse-any-number
      (<* spaces)
      (<=> parse-any-number)
      (<* spaces)
      (<=> (<|> parse-any-number (optional-parser 0.0)))
      (<f> new-tc)))

(def parse-tc-line
  (-> (parse-char \v)
      (*> (parse-char \t))
      (*> spaces)
      (*> parse-tc)))

(def parse-normal
  (-> parse-any-number
      (<* spaces)
      (<=> parse-any-number)
      (<* spaces)
      (<=> parse-any-number)
      (<f> new-normal)))

(def parse-normal-line
  (-> (parse-char \v)
      (*> (parse-char \n))
      (*> spaces)
      (*> parse-normal)))

(def parse-face-element
  (-> (<|> parse-any-int (optional-parser 0))
      (<* (parse-char \/))
      (<=> (<|> parse-any-int (optional-parser 0)))
      (<* (parse-char \/))
      (<=> (<|> parse-any-int (optional-parser 0)))
      (<f> new-face-element)))

(def parse-face-line
  (-> (parse-char \f)
      (*> spaces)
      (*> parse-face-element)
      (<* spaces)
      (<=> parse-face-element)
      (<* spaces)
      (<=> parse-face-element)
      (<f> new-face)))

(def parse-wavefront-line
  (-> parse-tc-line
      (<|> parse-normal-line)
      (<|> parse-vline)
      (<|> parse-face-line)))

(defn parse-file [file-path]
  (with-open [rdr (clojure.java.io/reader "./sample.txt")]
    (reduce (fn [acc ]))))

(defn parse-file [file-path]
  (group-by class
            (with-open [rdr (clojure.java.io/reader file-path)]
              (reduce (fn [acc ln]
                        (if-let [parsed-val (extract-value (parse-wavefront-line ln))]
                          (conj acc parsed-val)
                          acc))
                      [] (line-seq rdr)))))

(comment
  (parse-file "./examples/sample.txt")

  (parse-vline "v 3.2 2.01 -32.0")
  (parse-tc-line "vt 0.555 0.000 0.555")
  (parse-normal-line "vn 0.707 0.000 0.707")
  (parse-face-line "f -2//-2 -1//-1 -3//-3"))
