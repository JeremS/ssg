(ns fr.jeremyschoffen.ssg.test-common
  (:require
    [clojure.tools.build.api :as tb]
    [clojure.tools.build.tasks.copy :refer [default-ignores]]
    [datascript.core :as d]
    [fr.jeremyschoffen.ssg.db :as db]
    [fr.jeremyschoffen.java.nio.alpha.file :as fs]
    [fr.jeremyschoffen.prose.alpha.out.html.compiler :as compiler]
    [fr.jeremyschoffen.ssg.assets :as assets]
    [fr.jeremyschoffen.ssg.prose :as prose]))


;; -----------------------------------------------------------------------------
;; Various paths of example assets
;; -----------------------------------------------------------------------------

(def test-resources (fs/path "test-resources"))
(def asset-dir (fs/path test-resources "assets"))
(def example-dir1 (fs/path asset-dir "example-dir"))
(def example-dir2 (fs/path asset-dir "example-dir2"))
(def target (fs/path "target_test"))
(def target-assets (fs/path target "assets"))
(def target-example1 (fs/path target "assets" "example-dir"))
(def target-example2 (fs/path target "assets" "example-dir2"))



;; -----------------------------------------------------------------------------
;; Definition of assets
;; -----------------------------------------------------------------------------
(defn make-test-asset [name]
  (assets/asset-file
    (fs/path asset-dir name)
    (fs/path target-assets name)))


(def evaluator (prose/make-sci-evaluator))

(def test-asset-file1 (make-test-asset "asset1.txt"))
(def test-asset-file2 (make-test-asset "asset2.txt"))
(def test-asset-dir1 (assets/asset-dir example-dir1 target-example1))
(def test-asset-dir2 (assets/asset-dir example-dir2 target-example2
                                       {:ignores (conj default-ignores ".*\\.inv")}))


(def prose-document-path (fs/path test-resources "prose" "includes" "main.prose"))
(def prose-document-target (fs/path target "document" "includes.html"))

(def test-prose-file (assets/prose-document
                       prose-document-path
                       prose-document-target
                       (comp compiler/compile! evaluator)))


(def assets
  [test-asset-file1
   test-asset-file2
   test-asset-dir1
   test-asset-dir2
   test-prose-file])

;; -----------------------------------------------------------------------------
;; Expected assets when build is done
;; -----------------------------------------------------------------------------

(def expected-productions
  (concat
    [(:target test-asset-file1)
     (:target test-asset-file2)
     (:target test-prose-file)
     (->> "asset.txt"
          (fs/path (:target test-asset-dir2))
          str)]
    (for [p ["asset1.txt" "asset2.txt"]]
      (->> p
          (fs/path (:target test-asset-dir1))
          str))))

(def excluded-file
  (->> "do-not-copy.inv"
       (fs/path (:target test-asset-dir2))
       str))

;; -----------------------------------------------------------------------------
;; Database setup
;; -----------------------------------------------------------------------------
(def ^:dynamic *test-db* nil)

(defn make-db []
  (d/create-conn db/schema))


(defn database-fixture [f]
  (binding [*test-db* (make-db)]
    (f)))


;; -----------------------------------------------------------------------------
;; Cleaning test builds
;; -----------------------------------------------------------------------------
(defn clean-test-target! []
  (tb/delete {:path (str target)}))


(defn test-target-fixture [f]
  (f)
  (clean-test-target!))


