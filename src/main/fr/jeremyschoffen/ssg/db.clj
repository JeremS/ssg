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



(defn get-all-productions-ids [db]
  (db/q '[:find [?id ...]
          :where
          [?id :target _]]
         db))


(defn deps-for-id [db production-id]
  (db/q '[:find [?dep ...]
          :in $ ?id
          :where
          [?id :depends-on+ ?dep]]
        db production-id))


(defn get-outdated-productions [db changed-files]
  (db/q '[:find [?id ...]
          :in $ ?changed-files
          :where
          [?id :depends-on ?dep]
          [?dep :path ?path]
          [(contains? ?changed-files ?path)]]
        db
        (set changed-files)))




