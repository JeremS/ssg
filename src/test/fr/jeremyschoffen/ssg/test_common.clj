(ns fr.jeremyschoffen.ssg.test-common
  (:require
    [asami.core :as db]
    [fr.jeremyschoffen.java.nio.alpha.file :as fs]
    [fr.jeremyschoffen.prose.alpha.out.html.compiler :as compiler]
    [fr.jeremyschoffen.ssg.assets :as assets]
    [fr.jeremyschoffen.ssg.prose :as prose]
    [fr.jeremyschoffen.ssg.utils :as u]))



;; -----------------------------------------------------------------------------
;; Database setup
;; -----------------------------------------------------------------------------
(def db-uri"asami:mem://asset-test-db")


(defn conn []
  (db/connect db-uri))


(defn setup-db []
  (db/create-database db-uri))


(defn tear-down-db []
  (db/delete-database db-uri))


(defn database-fixture [f]
  (setup-db)
  (f)
  (tear-down-db))


;; -----------------------------------------------------------------------------
;; Various paths of example assets
;; -----------------------------------------------------------------------------

(def test-resources (fs/path "test-resources"))
(def asset-dir (fs/path test-resources "assets"))
(def example-dir (fs/path asset-dir "example-dir"))
(def target (fs/path "target_test"))
(def target-assets (fs/path target "assets"))
(def target-example (fs/path target "assets" "example-dir"))



;; -----------------------------------------------------------------------------
;; Definition of assets
;; -----------------------------------------------------------------------------
(defn make-test-asset-tx [name]
  (assets/asset-file
    (fs/path asset-dir name)
    (fs/path target-assets name)))


(def evaluator (prose/make-sci-evaluator))

(def assets
  (u/with-fresh-temp-ids
    (-> []
        (into (map make-test-asset-tx)
              ["asset1.txt" "asset2.txt"])
        (conj (assets/asset-dir example-dir target-example))
        (conj (assets/prose-document (fs/path test-resources "prose" "includes" "main.prose")
                                     (fs/path target "document" "includes.html")
                                     (comp compiler/compile! evaluator))))))




(comment
  (require '[portal.api :as portal] '[fr.jeremyschoffen.ssg.build :as build])
  (add-tap #'portal/submit)
  (tap> ::hello)
  (portal/open)
  (setup-db)
  (tear-down-db)
  (def res (db/transact (conn) {:tx-data assets}))
  (deref res)

  (tap>
    (let [db (db/db (conn))]
      (->> db
           assets/get-all-productions-ids
           (into []
                 (comp
                   (map #(u/entity db %))
                   (map build/build))))))

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

