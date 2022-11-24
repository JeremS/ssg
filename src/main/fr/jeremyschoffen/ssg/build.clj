(ns fr.jeremyschoffen.ssg.build
  (:require
    [datascript.core :as d]
    [fr.jeremyschoffen.ssg.db :as db]))


(defmulti entity->build-plan :type)
(defmulti build! (fn [_ spec] (:type spec)))


(defn generate-build-plan
 [specs]
 (sequence
   (map entity->build-plan)
   specs))


(defn execute-build! [conn build-plan]
  (mapv (partial build! conn) build-plan))


(defn build-all!
  ([conn]
   (->> conn
        d/db
        db/get-all-productions
        generate-build-plan
        (build-all! conn)))
  ([conn build-plan]
   (execute-build! conn build-plan)))



