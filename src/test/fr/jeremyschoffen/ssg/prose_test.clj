(ns fr.jeremyschoffen.ssg.prose-test
  (:require
    [clojure.test :as t :refer [deftest testing is]]
    [fr.jeremyschoffen.java.nio.alpha.file :as fs]
    [fr.jeremyschoffen.ssg.prose :as p]))

;; Testing the recuperation of deps when evaluating a prose document.

(def root (fs/path "test-resources/prose/includes"))

(def ev (p/make-evaluator {:root-dir root}))

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
  (let [res (p/eval&record-deps {:eval ev
                                 :root root
                                 :path "main.prose"})]
    (is (=  (:deps res) expected-deps))
    (is (= (:classification res) expected-classification))))



(comment
  (t/run-tests)
  *e)
