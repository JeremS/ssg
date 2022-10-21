(ns fr.jeremyschoffen.ssg.build-test
  (:require
    [asami.core :as db]
    [clojure.test :as t :refer [deftest is testing use-fixtures]]
    [clojure.tools.build.api :as tb]
    [fr.jeremyschoffen.ssg.build :as build]
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
  (deref (db/transact (common/conn) {:tx-data [common/assets]}))

  (require '[fr.jeremyschoffen.ssg.assets :as assets])
  (assets/get-all-productions-ids (common/conn))

  (assets/deps-for-id (common/conn) :a/node-38490)
  (db/entity (common/conn) :a/node-38490 true)

  (tap> ::hello)
  (build/build-all! (common/conn))
  *e


  (count(db/q '[:find ?id
                :where
                [?id :depends-on _]]
             (common/conn))))



