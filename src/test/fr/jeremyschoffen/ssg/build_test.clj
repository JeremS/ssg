(ns fr.jeremyschoffen.ssg.build-test
  (:require
    [clojure.test :as t :refer [deftest is testing use-fixtures]]
    [clojure.tools.build.api :as tb]
    [fr.jeremyschoffen.java.nio.alpha.file :as fs]
    [fr.jeremyschoffen.ssg.build :as build]
    [fr.jeremyschoffen.ssg.db :as db]
    [fr.jeremyschoffen.ssg.prose-test :as pt]
    [fr.jeremyschoffen.ssg.test-common :as common]))


(defn clean-test-target! []
  (tb/delete {:path (str common/target)}))

(defn test-target-fixture [f]
  (f)
  (clean-test-target!))


(use-fixtures :each common/database-fixture test-target-fixture)


(defn get-current-prose-test-doc-deps []
  (let [db (db/db (common/conn))
        id (db/q
             '[:find ?id .
               :in $ ?path
               :where
               [?id :src ?path]]
             db
             (str (pt/add-root "main.prose")))]
    (-> (db/entity db id true)
        :depends-on
        (->> (into #{} (map (comp str :path)))))))


(defn get-expected-paths-for-generated-files []
  (let [db (db/db (common/conn))]
    (->> db
         db/get-all-productions-ids
         (map (partial db/entity db))
         (map :target))))


(def expected-deps
  (-> pt/expected-deps
      (disj (pt/add-root "main.prose"))
      (->> (into #{} (map str)))))


(deftest example-build
  (deref (db/transact (common/conn) {:tx-data common/assets}))
  (build/build-all! (common/conn))

  (testing "Every file is created"
    (is (every? fs/exists? (get-expected-paths-for-generated-files))))

  (testing "Deps for the prose document are correctly inserted in the db."
    (is (= expected-deps
           (get-current-prose-test-doc-deps)))))


(comment
  (t/run-tests)

  (clean-test-target!)
  (common/setup-db)
  (common/tear-down-db)
  (deref (db/transact (common/conn) {:tx-data common/assets}))



  (build/build-all! (common/conn)))

 
