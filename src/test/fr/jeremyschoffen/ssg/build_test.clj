(ns fr.jeremyschoffen.ssg.build-test
  (:require
    [clojure.test :as t :refer [deftest is testing use-fixtures]]
    [clojure.tools.build.api :as tb]
    [datascript.core :as d]
    [fr.jeremyschoffen.java.nio.alpha.file :as fs]
    [fr.jeremyschoffen.ssg.build :as build]
    [fr.jeremyschoffen.ssg.db :as db]
    [fr.jeremyschoffen.ssg.prose-test :as pt]
    [fr.jeremyschoffen.ssg.test-common :as common]))


(use-fixtures :each common/database-fixture common/test-target-fixture)


(defn get-current-prose-test-doc-deps []
  (let [db (d/db common/*test-db*)
        deps (d/q
               '[:find (pull ?id [{:depends-on [:path]}]).
                 :in $ ?path
                 :where
                 [?id :src ?path]]
               db
               (str (pt/add-root "main.prose")))]
    (-> deps
        :depends-on
        (->> (into #{} (map (comp str :path)))))))


(defn get-expected-paths-for-generated-files []
  (let [db (d/db common/*test-db*)]
    (->> db
         db/get-all-productions
         (map :target))))


(def expected-deps
  (-> pt/expected-deps
      (disj (pt/add-root "main.prose"))
      (->> (into #{} (map str)))))


(deftest example-build
  (d/transact! common/*test-db* common/assets)
  (build/build-all! common/*test-db*)

  (testing "Every file is created"
    (is (every? fs/exists? common/expected-productions)))

  (testing ".inv file is excluded from example2"
    (is (not (fs/exists? common/excluded-file))))

  (testing "Deps for the prose document are correctly inserted in the db."
    (is (= expected-deps
           (get-current-prose-test-doc-deps)))))


(comment
  (t/run-tests)

  (common/clean-test-target!)

  (def conn (common/make-db))
  (d/transact! conn  common/assets)
  (build/build-all! conn)
  (build/generate-build-plan (db/get-all-productions (d/db conn)))
  *e

  (binding [common/*test-db* conn]
    (get-expected-paths-for-generated-files)))

