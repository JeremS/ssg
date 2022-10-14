(ns fr.jeremyschoffen.ssg.assets-test
  (:require
    [asami.core :as db]
    [clojure.test :as t :refer [deftest is testing]]
    [fr.jeremyschoffen.java.nio.alpha.file :as fs]
    [fr.jeremyschoffen.ssg.assets :as assets]
    [fr.jeremyschoffen.ssg.utils :as u]))



(def asset-dir (fs/path "test-resources/assets"))
(def target (fs/path "target_test"))
(def target-assets (fs/path target "assets"))


(def db-uri"asami:mem://asset-test-db")

(db/delete-database db-uri)
(db/create-database db-uri)


(def conn (db/connect db-uri))


(defn make-test-asset-tx [name]
  (assets/asset-file
    (fs/path asset-dir name)
    (fs/path target-assets name)))


(def assets
  (u/with-fresh-temp-ids
    (into []
          (map make-test-asset-tx)
          ["asset1.txt" "asset2.txt"])))



(comment

  (def res (db/transact conn {:tx-data assets}))
  (deref res)

  (map #(u/entity conn %) (assets/get-all-productions-ids (db/db conn)))

  (-> conn
      db/db
      assets/get-all-productions-ids
      (->> (map (fn [id]
                  {id (assets/deps-for-id (db/db conn) id)}))))

  (db/q '[:find ?id ?dep
          :where
          [?id :production/path]
          [?id :production/deps ?dep]]
        (db/db conn)))

