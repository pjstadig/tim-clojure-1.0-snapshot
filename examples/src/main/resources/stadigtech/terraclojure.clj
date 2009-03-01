(ns stadigtech.terraclojure
  (import (stadigtech.terraclojure Root)))

(def *hash* Root/hash)
(def *count* Root/count)

(defn add [key value]
  (alter *hash* assoc key value)
  (alter *count* inc))
