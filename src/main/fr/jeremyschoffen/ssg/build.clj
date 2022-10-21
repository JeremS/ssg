(ns fr.jeremyschoffen.ssg.build
  (:require
    [asami.core :as db]
    [fr.jeremyschoffen.ssg.assets :as assets]
    [fr.jeremyschoffen.ssg.utils :as u]))


(defmulti entity->build-plan :type)
(defmulti build! (fn [conn spec] (:type spec)))






(defn deps-for-id [db production-id]
  (db/q '[:find [?dep ...]
          :in $ ?id
          :where
          [?id :depends-on+ ?dep]]
        db production-id))


(defn generate-build-plan [conn]
  (let [db (db/db conn)
        productions (assets/get-all-productions-ids db)]
    (sequence
      (comp
        (map (partial u/entity db))
        (map entity->build-plan))
      (assets/get-all-productions-ids db))))



(defn execute-build! [conn build-plan]
  (mapv (partial build! conn) build-plan))



(defn build-all! [conn]
  (let [build-plan (generate-build-plan conn)]
    (execute-build! conn build-plan)))



