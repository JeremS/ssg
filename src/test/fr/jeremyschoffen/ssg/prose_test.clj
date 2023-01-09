(ns fr.jeremyschoffen.ssg.prose-test
  (:require
    [clojure.test :as t :refer [deftest testing is]]
    [fr.jeremyschoffen.java.nio.alpha.file :as fs]
    [fr.jeremyschoffen.ssg.prose :as p]
    [fr.jeremyschoffen.ssg.test-common :as common]))

;; Testing the recuperation of deps when evaluating a prose document.

(def root (fs/path "test-resources/prose/includes"))

(def clojure-eval (common/make-clojure-evaluator))

(def sci-eval (common/make-sci-evaluator))

(def add-root (partial fs/path root))
(def normalize-path add-root)

(def expected-deps
  #{(normalize-path "main.prose")
    (normalize-path "insert.prose")
    (normalize-path "insert/iinsert.prose")
    (normalize-path "insert/iinsert2.prose")
    (normalize-path "require.prose")
    (normalize-path "require/rinsert.prose")
    (normalize-path "require/rrequire.prose")})

(deftest tracking-deps
  (testing "In clojure env"
    (let [res (p/eval&record-deps {:eval clojure-eval
                                   :eval-env {::common/root-dir root}
                                   :path "main.prose"})]
      (is (=  (-> res :deps set) expected-deps))))

  (testing "In sci env"
    (let [res (p/eval&record-deps {:eval sci-eval
                                   :eval-env {::common/root-dir root}
                                   :path "main.prose"})]
      (is (=  (-> res :deps set) expected-deps)))))

(comment
  (clojure-eval "main.prose" {::common/root-dir root})
  (sci-eval "main.prose" {::common/root-dir root})
  (p/eval&record-deps {:eval sci-eval
                       :eval-env {::common/root-dir root}
                       :path "main.prose"})
  (t/run-tests)
  *e)

