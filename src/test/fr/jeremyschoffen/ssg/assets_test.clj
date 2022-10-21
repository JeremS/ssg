(ns fr.jeremyschoffen.ssg.assets-test
  (:require
    [asami.core :as db]
    [clojure.test :as t :refer [deftest is testing use-fixtures]]
    [fr.jeremyschoffen.ssg.assets :as assets]
    [fr.jeremyschoffen.ssg.test-common :as common]))







(use-fixtures :each common/database-fixture)




(defn get-assets []
  (set common/assets))


(defn get-productions []
  (let [db (db/db (common/conn))]
    (->> db
         assets/get-all-productions-ids
         (into #{} (map (partial db/entity db))))))

(deftest example-assets
  (deref (db/transact (common/conn) {:tx-data common/assets}))
  (is (= (get-productions) (get-assets))))



(comment
  (t/run-tests))







