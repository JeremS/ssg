(ns fr.jeremyschoffen.ssg.prose.utils)


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

