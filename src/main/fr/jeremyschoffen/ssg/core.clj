(ns fr.jeremyschoffen.ssg.core
  (:require
    [asami.core :as db]
    [fr.jeremyschoffen.java.nio.alpha.file :as fs]))


(def db-uri"asami:mem://dbtest")
(db/create-database db-uri)


(def conn (db/connect db-uri))

(def assets [{:db/id -1
              :asset/path (fs/path "toto" "titi")}
             {:production/path (fs/path "dest" "titi")
              :production/src [-1]}])

(def res (db/transact conn {:tx-data assets}))


(comment
  (db/q '[:find [?id]
          :where [_ :production/src ?id]]
        (db/db conn)))

