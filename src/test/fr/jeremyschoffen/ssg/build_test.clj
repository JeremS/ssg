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


  (db/get-all-productions-ids (common/conn))

  (db/deps-for-id (common/conn) :a/node-38209)
  (db/entity (common/conn) :a/node-38209 true)

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

  (count  (db/q '[:find [?id ...]
                  :where
                  [?id :depends-on _]]
               (db/db (common/conn)))))

