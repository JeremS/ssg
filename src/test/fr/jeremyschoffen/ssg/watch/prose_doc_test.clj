(ns fr.jeremyschoffen.ssg.watch.prose-doc-test
  (:require
    [clojure.test :as t :refer [deftest is testing]]
    [datascript.core :as d]
    [fr.jeremyschoffen.ssg.assets :as assets]
    [fr.jeremyschoffen.ssg.build :as build]
    [fr.jeremyschoffen.ssg.prose-test :as pt]
    [fr.jeremyschoffen.ssg.test-common :as common]))

(t/use-fixtures :each common/database-fixture common/test-target-fixture)

(def prose-document-path (str common/prose-document-path))
(def dependencies-paths (disj (into #{} (map str) pt/expected-deps)
                              prose-document-path))


(def dumy-file-name "dumy.prose")


(deftest prose-documents
  (d/transact! common/*test-db* common/assets)
  (build/build-all! common/*test-db*)
  (d/transact! common/*test-db* [{:type :fr.jeremyschoffen.ssg.assets.prose-doc/prose-doc :src dumy-file-name}])


  (testing "Detects changes in main document."
    (is (= (str common/prose-document-path)
           (-> (assets/get-outdated-prose-docs (d/db common/*test-db*)
                                               #{prose-document-path})
               first
               :src
               str)))
    (is (= (str dumy-file-name)
           (-> (assets/get-outdated-prose-docs (d/db common/*test-db*)
                                               #{dumy-file-name})
               first
               :src
               str))))
  (testing "Detects changes in dependencies"
    (is (= (str common/prose-document-path)
           (-> (assets/get-outdated-prose-docs (d/db common/*test-db*)
                                               dependencies-paths)
               first
               :src
               str)))))
(comment
  (t/run-tests))

