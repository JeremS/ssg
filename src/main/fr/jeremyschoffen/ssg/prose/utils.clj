(ns fr.jeremyschoffen.ssg.prose.utils
  (:require
    [fr.jeremyschoffen.java.nio.alpha.file :as fs]
    [hyperfiddle.rcf :refer [tests]]
    [meander.epsilon :as m]
    [medley.core :as med]))


;;-----------------------------------------------------------------------------
;; Path utilities
;;-----------------------------------------------------------------------------
(defn add-root [path root]
  (fs/path root path))


(defn remove-root [path root]
  (fs/relativize root path))


(tests
  (def r (fs/path "toto" "tata"))
  (def p (fs/path "titi"))
  (def p-long (fs/path "toto" "tata""titi"))


  (add-root p r) := p-long
  (remove-root p-long r) := p)


(defn normalize-path [root path]
  (->> path
       (fs/path root)
       fs/normalize))


;;-----------------------------------------------------------------------------
;; Recording deps utilities
;;-----------------------------------------------------------------------------
(def ^:dynamic *deps-record* nil)


(defmacro record-in [r & body]
  `(binding [*deps-record* ~r]
     (do ~@body)))


(defn make-empty-recording []
  (atom []))

(defn record [v]
  (when *deps-record*
    (swap! *deps-record* conj v)))

