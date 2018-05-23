# clj-parser

This is a simple parser designed for high reusability and composibility.

## What is a parser

A parser is a simple function that takes an input string, and performs some computation on that string.

When the parsing is successful, the function returns a vector with two elements. The first element is the parsed value, and the second element is the remaining string.

On unsuccessful parsing, the return value is `nil`

## Usage

Here is an example of a parser -

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

If you let your imagination run wild, you can begin to see how this way of creating parsers could get tedious and repetitive. To keep things DRY there is a helper function called `satisfy`. Lets recreate the above parser using `satisfy` -

```clj
(def first-letter-should-be-A-revisited [s]
  (satisfy #(= % \A)))

(first-letter-should-be-A-revisited "A beautiful sunset")
;; => ["A" " beautiful sunset"]
(first-letter-should-be-A-revisited "The wind whistles")
;; => nil
```

This looks much nicer and cleaner. So `satisfy` is a function, which takes a predicate, and checks if the first character of the given string satisfies the predicate. With `satisfy` we can build some really useful parsers. The functions `parse-char`, `digit` and `alphanumeric` have been implemented using the `satisfy` function.

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

There are a couple of other helper functions that are useful. `one-or-more` takes a parser and runs that parser successfully until it can't run it anymore. A caveat is that this function requires the parser to successfully run **at aleast** once. Lets look at an example

```clj
(def number (one-or-more digit))

(number "123 abc")
;; => ["123" " abc"]

(number "abc")
;; => nil

```

`zero-or-more` is like the function `one-or-more` except it allows the parser to be unsuccessful, and returns an empty string if completely unsuccessful. For example -

```clj
(def spaces (zero-or-more (parse-char \ )))

(spaces "   123")
;; => ["   " "123"]

(spaces "123")
;; => ["" "123"]
```

### Composing Parsers

Now that we have some basic building blocks in place to parse characters, it would be nice to be able to combine them to build more powerful parsers. For example, lets say I want to parse the characters "LOG: " out of log line. This is how we would do it -

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

`<=>` behaves differently based on return type of the parsed value. If the first parser returns a string, then the result of the first parser is concatenated with the second parser. For example -

```clj
((<=> (parse-char \a) (parse-char \b)) "abcd")
;; => ["ab" "cd"]

If the first parser returns a non-stringy value, an array containing the two values are returned.

((<=> parse-pos-int (parse-word "hello")) "123hello")
;; => [[123 "hello"] ""]

((-> (parse-word "aloha")
     (<=> parse-pos-int)
     (<=>  (parse-word "hello")))
     "aloha123hello")

;; => [["aloha" 123 "hello"] ""]
```

### Composing Either

Sometimes we want to use two parsers, and proceed if either one succeeds. To make that happen we have the function `compose-or` or the symbol `<|>`. Lets look at an example -

```clj
;; Suppose we want to parse log files which begin with "LOG: " or with "WARN: ".
;; We want to be able to parse both of them out. We shall create two parsers and
;; combine them using our `compose-or` function.

(def parse-log (parse-word "LOG: "))

(def parse-warn (parse-word "WARN: "))

(def parse-log-line
    (<|> parse-log parse-warn))

(parse-log-line "LOG: abcd")
;; => ["LOG: " "abcd"]

(parse-log-line "WARN: hello")
;; => ["WARN: " "hello"]
```

### Composing Right

Sometimes we want to discard the results of our first parser, and we are only interested in the results of the second parser. The function `compose-right` or `*>` does exactly that. For example, we may want to eliminate spaces before parsing. Here is an example -

```clj
(def parse-log (parse-log "LOG: "))

(def parse-log-line
    (*> spaces parse-log))

(parse-log-line "  LOG: The water makes its journey...")
;; => ["LOG: " "The water makes its journey..."]
```

### Composing Left

Similar to `compose-right`, but instead the function `compose-left` or `<*` discards the results of the second parser, and we are only interested in the results of the first parser. For example, we may want to strip the spaces at the end of the string. Here is an example -

```clj
;; If we want to trim the whitespace after parsing a word

(def parse-word-no-whitespaces
    (<* word spaces ))

(parse-word-no-whitespaces "Wooordddd    ")
;; => ["Wooordddd" ""]
```

### Compose a function

After parsing, we may want to apply a function to the parsed string. This is where we use `compose-apply` or `<f>`. This is especially useful when we want to convert the parsed string into a different datatype. Lets look at an example -

```clj

(def s "123.23")
(any-number "123.23 abc")
;; => ["123.12" " abc"]

;; This is parsed out as a string, it would be nice if this could be parsed as
;; a `Double` value.

(def double-parser (<f> any-number #(Double/parseDouble %)))
(double-parser "123.23 abc")
;; => [123.23 " abc"]
```

For a more extravagant example, look at the [Wavefront Format Parser](./examples/wavefront_parser.clj).

## License

Copyright © 2018 Punit Rathore

Distributed under the MIT License.
