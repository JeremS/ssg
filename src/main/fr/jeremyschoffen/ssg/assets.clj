(ns fr.jeremyschoffen.ssg.assets
  (:require
    [asami.core :as db]
    [fr.jeremyschoffen.dolly.core :as dolly]
    [fr.jeremyschoffen.ssg.assets.file :as af]
    [fr.jeremyschoffen.ssg.assets.dir :as ad]
    [fr.jeremyschoffen.ssg.assets.prose-doc :as ap]))


(dolly/def-clone asset-file af/make)

(dolly/def-clone asset-dir ad/make)

(dolly/def-clone prose-document ap/make)


(comment
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
          db production-id)))




