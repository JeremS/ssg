(ns fr.jeremyschoffen.ssg.build-test
  (:require
    [clojure.test :as t :refer [deftest is testing use-fixtures]]
    [clojure.tools.build.api :as tb]
    [fr.jeremyschoffen.ssg.build :as build]
    [fr.jeremyschoffen.ssg.db :as db]
    [fr.jeremyschoffen.ssg.test-common :as common]))


(defn clean-test-target! []
  (tb/delete {:path (str common/target)}))

(defn test-target-fixture [f]
  (f)
  (clean-test-target!))


(use-fixtures :each common/database-fixture test-target-fixture)




(comment
  (deftest example-build
    (deref (db/transact (common/conn) {:tx-data common/assets}))))


(comment
  (require '[portal.api :as portal])
  (portal/open)
  (portal/clear)
  (add-tap #'portal/submit)
  (t/run-tests)

  (clean-test-target!)
  (common/setup-db)
  (common/tear-down-db)
  (deref (db/transact (common/conn) {:tx-data [common/test-prose-file]}))


  (-> (common/conn)
      (db/get-all-productions-ids)
      first
      (as-> id (db/entity (common/conn) id))
      (-> :depends-on
          (->> (map :db/ident))))
          ;     (map (partial db/entity (common/conn))))))

  (db/entity (common/conn)
             (first
               (db/get-all-productions-ids (common/conn))))



  (build/generate-build-plan (common/conn))
  (tap> ::hello)
  (build/build-all! (common/conn))

  *e


  (let [conn (common/conn)
        db (db/db conn)]
    (map
      #(db/entity db %)
      (db/q '[:find [?id ...]
              :where
              [?id :depends-on _]]
            db)))

  (->> (db/q '[:find [?id ...]
                   :where
                   [?id :type :prose-dependency]]
              (db/db (common/conn)))
       (map (partial db/entity (common/conn)))
       (map :path)
       sort))

