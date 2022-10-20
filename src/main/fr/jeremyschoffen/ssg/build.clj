(ns fr.jeremyschoffen.ssg.build
  (:require
    [asami.core :as db]
    [fr.jeremyschoffen.ssg.utils :as u]))


(defmulti build :type)
(defmulti build! (fn [conn spec] (:type spec)))




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


(defn generate-build [conn]
  (let [db (db/db conn)
        productions (get-all-productions-ids db)]
    (map (partial u/entity db) productions)))



(defn execute-build! [conn build-plan]
  (mapv (partial build! conn) build-plan))



(defn build-all! [conn]
  (let [build-plan (generate-build conn)]
    (execute-build! conn build-plan)))



