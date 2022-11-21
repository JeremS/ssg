(ns fr.jeremyschoffen.ssg.build
  (:require
    [fr.jeremyschoffen.ssg.db :as db]))


(defmulti entity->build-plan :type)
(defmulti build! (fn [conn spec] (:type spec)))


(defn generate-build-plan
 ([db]
  (generate-build-plan db (db/get-all-productions-ids db)))
 ([db ids]
  (sequence
    (comp
      (map #(db/entity db % true))
      (map entity->build-plan))
    ids)))


(defn execute-build! [conn build-plan]
  (mapv (partial build! conn) build-plan))


(defn build-all!
  ([conn]
   (build-all! conn (generate-build-plan (db/db conn))))
  ([conn build-plan]
   (execute-build! conn build-plan)))



