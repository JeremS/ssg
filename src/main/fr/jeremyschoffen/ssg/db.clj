(ns fr.jeremyschoffen.ssg.db
  (:require
    [asami.core :as db]
    [fr.jeremyschoffen.dolly.core :as dolly]))

(dolly/add-keys-to-quote! :raw-arglist :raw-arglists)

(dolly/def-clone db/connect)

(dolly/def-clone db/db)

(dolly/def-clone db/q)

(dolly/def-clone db/create-database)

(dolly/def-clone db/delete-database)

(dolly/def-clone db/transact)


(defn entity
  ([db id]
   (assoc (db/entity db id) :db/id id))
  ([db id nested?]
   (assoc (db/entity db id nested?) :db/id id)))



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


