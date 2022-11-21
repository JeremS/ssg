(ns fr.jeremyschoffen.ssg.watch-tests
  (:require
    [clojure.test :as t :refer [deftest is testing use-fixtures]]
    [fr.jeremyschoffen.java.nio.alpha.file :as fs]
    [fr.jeremyschoffen.ssg.build :as build]
    [fr.jeremyschoffen.ssg.db :as db]
    [fr.jeremyschoffen.ssg.prose-test :as pt]
    [fr.jeremyschoffen.ssg.test-common :as common]))



(comment
  (common/setup-db)
  (common/tear-down-db)

  (deref (db/transact (common/conn) {:tx-data common/assets}))
  (build/build-all! (common/conn))

  (db/get-all-productions-ids (common/conn))
  (db/get-outdated-productions (common/conn) (into #{} (map str) pt/expected-deps))
  (build/generate-build-plan (db/db (common/conn))
                             (db/get-outdated-productions (common/conn) (into #{} (map str) pt/expected-deps))))
