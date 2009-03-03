(ns stadigtech.terraclojure
  (import (stadigtech.terraclojure Root)))

(def *hash* (ref {}))
(def *count* (ref 0))

(defn add [key value]
  (alter *hash* assoc key value)
  (alter *count* inc))
