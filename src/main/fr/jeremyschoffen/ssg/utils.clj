(ns fr.jeremyschoffen.ssg.utils
  (:require
    [asami.core :as db]
    [hyperfiddle.rcf :refer [tests]]))

(defn entity
  ([db id]
   (assoc (db/entity db id) :db/id id))
  ([db id nested?]
   (assoc (db/entity db id nested?) :db/id id)))

(defn fresh-temp-id [] (atom 0))

(def ^:dynamic *next-id* (fresh-temp-id))


(defn next-id []
  (swap! *next-id* dec))


(defmacro with-fresh-temp-ids [& body]
  `(binding [*next-id* (fresh-temp-id)]
     ~@body))



(defn temp-id-maker []
  (let [current (atom 0)]
    (fn next-temp-id []
      (swap! current dec))))

(tests
  (def temp-id (temp-id-maker))
  (temp-id) := -1
  (temp-id) := -2
  (temp-id) := -3)

