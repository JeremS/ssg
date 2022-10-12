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

(def expected-deps
  {(add-root "main.prose") #{(add-root "require.prose") (add-root "insert.prose")}
   (add-root "insert.prose")  #{(add-root "insert/iinsert2.prose")
                                (add-root "insert/iinsert.prose")}
   (add-root "require.prose") #{(add-root "require/rinsert.prose")}
   (add-root "require/rinsert.prose") #{(add-root "require/rrequire.prose")}})


(def expected-classification
  {(add-root "insert.prose") :insert
   (add-root "insert/iinsert.prose") :insert
   (add-root "insert/iinsert2.prose") :insert
   (add-root "require.prose") :require
   (add-root "require/rinsert.prose") :insert
   (add-root "require/rrequire.prose") :require})


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
