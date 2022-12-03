(ns fr.jeremyschoffen.ssg.watch.file-test
  (:require
    [clojure.test :as t :refer [deftest is testing]]
    [datascript.core :as d]
    [fr.jeremyschoffen.ssg.assets :as assets]
    [fr.jeremyschoffen.ssg.test-common :as common]))


(t/use-fixtures :each common/database-fixture)



(def test-asset-path (:src common/test-asset-file1))

(deftest prose-documents
  (d/transact! common/*test-db* common/assets)


  (testing "Detects changes in main document."
    (is (= (str test-asset-path)
           (-> common/*test-db*
               d/db
               (assets/get-outdated-asset-files #{test-asset-path})
               first
               :src
               str)))))

(comment
  (t/run-tests))

