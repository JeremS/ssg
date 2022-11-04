(ns fr.jeremyschoffen.ssg.build
  (:require
    [fr.jeremyschoffen.ssg.db :as db]))


(defmulti entity->build-plan :type)
(defmulti build! (fn [conn spec] (:type spec)))




(defn generate-build-plan [conn]
  (let [db (db/db conn)]
    (sequence
      (comp
        (map (partial db/entity db))
        (map entity->build-plan))
      (db/get-all-productions-ids db))))



(defn execute-build! [conn build-plan]
  (mapv (partial build! conn) build-plan))



(defn build-all! [conn]
  (let [build-plan (generate-build-plan conn)]
    (execute-build! conn build-plan)))



