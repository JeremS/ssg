(ns fr.jeremyschoffen.ssg.test-common
  (:require
    [clojure.tools.build.api :as tb]
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
(def example-dir (fs/path asset-dir "example-dir"))
(def target (fs/path "target_test"))
(def target-assets (fs/path target "assets"))
(def target-example (fs/path target "assets" "example-dir"))



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
(def test-asset-dir (assets/asset-dir example-dir target-example))


(def prose-document-path (fs/path test-resources "prose" "includes" "main.prose"))
(def prose-document-target (fs/path target "document" "includes.html"))

(def test-prose-file (assets/prose-document
                       prose-document-path
                       prose-document-target
                       (comp compiler/compile! evaluator)))


(def assets
  [test-asset-file1 test-asset-file2 test-asset-dir test-prose-file])



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


