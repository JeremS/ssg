(ns fr.jeremyschoffen.ssg.prose-test
  (:require
    [clojure.test :as t :refer [deftest testing is]]
    [fr.jeremyschoffen.java.nio.alpha.file :as fs]
    [fr.jeremyschoffen.ssg.prose :as p]))

;; Testing the recuperation of deps when evaluating a prose document.

(def root (fs/path "test-resources/prose/includes"))

(def clojure-eval (p/make-evaluator))

(def sci-eval (p/make-sci-evaluator))

(def add-root (partial fs/path root))
(def normalize-path (comp str add-root))

(def expected-deps
  {(normalize-path "main.prose") #{(normalize-path "require.prose")
                                   (normalize-path "insert.prose")}
   (normalize-path "insert.prose")  #{(normalize-path "insert/iinsert2.prose")
                                      (normalize-path "insert/iinsert.prose")}
   (normalize-path "require.prose") #{(normalize-path "require/rinsert.prose")}
   (normalize-path "require/rinsert.prose") #{(normalize-path "require/rrequire.prose")}})


(def expected-classification
  {(normalize-path "insert.prose") :insert
   (normalize-path "insert/iinsert.prose") :insert
   (normalize-path "insert/iinsert2.prose") :insert
   (normalize-path "require.prose") :require
   (normalize-path "require/rinsert.prose") :insert
   (normalize-path "require/rrequire.prose") :require})


(deftest tracking-deps
  (testing "In clojure env"
    (let [res (p/eval&record-deps {:eval clojure-eval
                                   :root root
                                   :path "main.prose"})]
      (is (=  (:deps res) expected-deps))
      (is (= (:classification res) expected-classification))))

  (testing "In sci env"
    (let [res (p/eval&record-deps {:eval sci-eval
                                   :root root
                                   :path "main.prose"})]
      (is (=  (:deps res) expected-deps))
      (is (= (:classification res) expected-classification)))))



(comment
  (clojure-eval "main.prose" {::p/root-dir root})
  (sci-eval "main.prose" {::p/root-dir root})
  (t/run-tests)
  *e)
