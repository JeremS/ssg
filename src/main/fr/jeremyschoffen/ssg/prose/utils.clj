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


(defn make-path-normalizer [root]
  (fn [path]
    (-> path
        (add-root root)
        fs/normalize)))


;;-----------------------------------------------------------------------------
;; Recording deps utilities
;;-----------------------------------------------------------------------------
(def ^:dynamic *deps-record* nil)


(defmacro record-in [r & body]
  `(binding [*deps-record* ~r]
     (do ~@body)))


(defn make-empty-recording []
  (atom []))


(defn enter-deps [d t]
  (when *deps-record*
    (swap! *deps-record* conj {:enter d :type t})))


(defn leave-deps [d]
  (when *deps-record*
    (swap! *deps-record* conj {:leave d})))


#_:clj-kondo/ignore
(defn recording->graph-step [state]
  (m/match state
    ;; Termination case
    {:records (m/pred empty?) :as ?state}
    ?state

    ;; enter case
    {:stack ?s
     :records (m/seqable (m/pred #(contains? % :enter) {:enter ?p})
                         & ?records)
     :res ?res}

    {:stack (conj ?s ?p)
     :records ?records
     :res (update ?res (peek ?s) (fnil conj #{}) ?p)}

    ;; leave case
    {:stack ?s
     :records (m/seqable (m/pred #(contains? % :leave) {:leave ?p})
                         & ?records)
     :res ?res}

    {:stack (pop ?s)
     :records  ?records
     :res ?res}))


(defn recording->graph [main-doc rec]
  (loop [state {:stack [main-doc]
                :records rec
                :res {}}]
    (let [state' (recording->graph-step state)]
      (if (identical? state' state)
        (:res state)
        (recur state')))))


(tests
  (def ex1
    {:stack [:main-doc]
     :records [{:enter :a}{:leave :a}]
     :res {}})

  (-> ex1
      recording->graph-step)
  := {:stack [:main-doc :a]
      :records '({:leave :a})
      :res {:main-doc #{:a}}}

  (-> ex1
      recording->graph-step
      recording->graph-step)
  := {:stack [:main-doc]
      :records '()
      :res {:main-doc #{:a}}}

  (-> ex1
      recording->graph-step
      recording->graph-step
      recording->graph-step)
  := {:stack [:main-doc]
      :records '()
      :res {:main-doc #{:a}}}

  (recording->graph :main-doc (:records ex1))
  := {:main-doc #{:a}})



(defn normalize-recording [recording root]
  (let [normalize-path (make-path-normalizer root)]
    (med/map-kv (fn [k v]
                  [(normalize-path k) (into #{} (map normalize-path) v)]) recording)))


(defn classify-dep [{:keys [enter type]}]
  [enter type])

(defn classify-deps [recording]
  (into {}
        (comp
          (filter :enter)
          (map classify-dep))
        recording))


(defn normalize-classification [classification root]
  (let [normalize-path (make-path-normalizer root)]
    (update-keys classification normalize-path)))

(tests
  (classify-deps [{:enter :a :type :insert}{:leave :a}{:enter :b :type :require}{:enter :c :type :require}{:leave :c}{:leave :b}])
  := {:a :insert :b :require :c :require})


