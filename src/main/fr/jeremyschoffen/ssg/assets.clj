(ns fr.jeremyschoffen.ssg.assets
  (:require
    [asami.core :as db]
    [fr.jeremyschoffen.dolly.core :as dolly]
    [fr.jeremyschoffen.ssg.assets.file :as af]
    [fr.jeremyschoffen.ssg.assets.dir :as ad]
    [fr.jeremyschoffen.ssg.assets.prose-doc :as ap]
    [fr.jeremyschoffen.ssg.utils :as u]))


(dolly/def-clone asset-file af/make)

(dolly/def-clone asset-dir ad/make)

(dolly/def-clone prose-document ap/make)



(comment
  (defn src-file [path]
    (let [id (u/next-id)]
      {:type :src-file
       :src/path path}))


  (defn production-file [path]
    (let [id (u/next-id)]
      {:type :production-file
       :dest/path path}))


  (defn make-dependencies-tx [prod-id deps-ids]
    (mapv (fn [id]
            [:db/add prod-id :depends-on id])
          deps-ids))


  (defn simple-asset-tx [src-path dest-path]
    (let [src-id (u/next-id)
          dest-id (u/next-id)
          tx-data (mapv #(assoc %1 :db/id %2)
                        [(src-file src-path) (production-file dest-path)]
                        [src-id dest-id])]
      (into tx-data (make-dependencies-tx dest-id [src-id])))))



(comment
  (src-file "toto/titi")
  (u/with-fresh-temp-ids
    (simple-asset-tx "toto/titi" "dest/tutu")))


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




