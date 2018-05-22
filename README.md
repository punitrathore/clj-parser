# clj-parser

This is a simple parser designed for high reusability and composibility.

## Usage

### What is a parser

A parser is a simple function that takes an input string, and performs some computation on that string.

When the parsing is successful, the function returns a vector with two elements. The first element is the parsed value, and the second element is the remaining string.

On unsuccessful parsing, the return value is `nil`

This should be clearer with an example

```clj
(defn first-letter-should-be-A [s]
  (let [first-letter (first s)]
    (if (= first-letter \A)
      [(str first-letter) (apply str (rest s))])))

(first-letter-should-be-A "A beautiful sunset")
;; => ["A" " beautiful sunset"]
(first-letter-should-be-A "The wind whistles")
;; => nil
```

So we have created a simple parser which checks if the string starts with the character `A`.

If you let your imagination run wild, you can begin to see how this way of creating parsers could get tedious and repetitive. To keep things DRY there is a helper function called `satisfy`. Lets see how to create the same parser using it.

```clj
(def first-letter-should-be-A-revisited [s]
  (satisfy #(= % \A)))

(first-letter-should-be-A-revisited "A beautiful sunset")
;; => ["A" " beautiful sunset"]
(first-letter-should-be-A-revisited "The wind whistles")
;; => nil
```

This looks much nicer and cleaner. So `satisfy` is a function, which takes a predicate, and checks if the first character of the given string satisfies the predicate. With `satisfy` we can build some really useful parsers. The function `parse-char`. `digit` and `alphanumeric` have been implemented using the `satisfy` function.

```clj
((parse-char \B)) "Blue sky")
;; => ["B" "lue sky"]

(digit "12abc")
;; => ["1" "2abc"]

(alphanumeric "abc")
;; => ["a" "bc"]
(alphanumeric "123")
;; => ["1" "23"]
```

### Composing Parsers

Now that we have some basic building blocks in place to parse characters, it would be nice to be able to combine them to build more powerful parsers. For example, lets say I want to parse the characters "LOG: " out of the string which begins with log. This is how we would do it

```clj

(def parse-log
    (-> (parse-char \L)
        (compose-parsers (parse-char \O))
        (compose-parsers (parse-char \G))
        (compose-parsers (parse-char \:))
        (compose-parsers (parse-char \ ))))

(parse-log "LOG: abcd")
;; => ["LOG: " "abcd"]
```
Since writing `compose-parsers` can be a little tedious for us lazy folks, you can also use the function `<=>` to compose two parsers. It kinda looks like a pipe between two parsers!

```clj
(def parse-log
    (-> (parse-char \L)
        (<=> (parse-char \O))
        (<=> (parse-char \G))
        (<=> (parse-char \:))
        (<=> (parse-char \ ))))

(parse-log "LOG: abcd")
;; => ["LOG: " "abcd"]
```

## License

Copyright © 2018 Punit Rathore

Distributed under the MIT License.
