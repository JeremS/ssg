(ns fr.jeremyschoffen.ssg.db
  (:require
    [datascript.core :as db]))


(defn fresh-temp-id [] (atom 0))


(def ^:dynamic *next-id* (fresh-temp-id))


(defn next-id []
  (swap! *next-id* dec))


(defmacro with-fresh-temp-ids [& body]
  `(binding [*next-id* (fresh-temp-id)]
     ~@body))


(def schema {:src {:db/unique :db.unique/identity}
             :target {:db/unique :db.unique/identity}
             :depends-on {:db/valueType :db.type/ref
                          :db/cardinality :db.cardinality/many}})


(defn get-all-productions [db]
  (db/q '[:find [(pull ?id [:*]) ...]
          :where
          [?id :target _]]
         db))

