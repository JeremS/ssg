(ns fr.jeremyschoffen.ssg.assets-test
  (:require
    [datascript.core :as db]
    [clojure.test :as t :refer [deftest is use-fixtures]]
    [fr.jeremyschoffen.ssg.db :as req]
    [fr.jeremyschoffen.ssg.test-common :as common]))


(use-fixtures :each common/database-fixture)


(defn get-assets []
  (set common/assets))


(defn get-productions []
  (->> common/*test-db*
       db/db
       req/get-all-productions
       (into #{} (map #(dissoc % :db/id)))))


(deftest example-assets
  (db/transact! common/*test-db* common/assets)
  (is (= (get-productions) (get-assets))))


(comment
  (t/run-tests))

