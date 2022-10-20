(ns fr.jeremyschoffen.ssg.build-test
  (:require
    [asami.core :as db]
    [clojure.test :refer [deftest is testing use-fixtures]]
    [clojure.tools.build.api :as tb]
    [fr.jeremyschoffen.ssg.test-common :as common]))



(defn clean-test-target! []
  (tb/delete {:path common/target}))

(defn test-target-fixture [f]
  (f)
  (clean-test-target!))


(use-fixtures :each common/database-fixture test-target-fixture)

(deftest example-build)




