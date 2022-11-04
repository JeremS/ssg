(ns fr.jeremyschoffen.ssg.assets.prose-doc
  (:require
    [meander.epsilon :as m]
    [clojure.tools.build.api :as tb]
    [fr.jeremyschoffen.ssg.db :as db]
    [fr.jeremyschoffen.java.nio.alpha.file :as fs]
    [fr.jeremyschoffen.ssg.build :as build]
    [fr.jeremyschoffen.ssg.prose :as p]
    [fr.jeremyschoffen.ssg.utils :as u]))



(defn make [src-path dest-path eval-fn & _]
  {:type ::prose-doc
   :src (str src-path)
   :target (str dest-path)
   :eval-fn eval-fn})


(defn make-tx-ids [main-doc-path main-doc-id classification]
  (-> classification
      (->> (reduce-kv
             (fn [acc k _]
               (assoc acc k (u/next-id)))
             {}))
      (assoc main-doc-path main-doc-id)))


(defn recording->deps-entities-tx [classification path->id]
  (into []
        (map (fn [[path t]]
               {:db/id (path->id path)
                :type :prose-dependency
                :prose-dependency-type t
                :path (str path)}))
        classification))


(defn recording->deps-tx [path->deps-map path->id]
  (let [res (m/rewrites path->deps-map
              {?path (m/seqable !dep ...)}
              [[:db/add (m/app path->id ?path)
                :depends-on (m/app path->id !dep)] ...])]
    (apply concat res)))


(defn recording->tx [main-doc-path main-doc-id {:keys [deps classification]}]
  (let [path->id (make-tx-ids (fs/path main-doc-path)
                              main-doc-id
                              classification)]
    (into (recording->deps-entities-tx classification path->id)
          (recording->deps-tx deps  path->id))))


(defn build [{:keys [src target eval-fn]
              :db/keys [id]}]
  (let [{:keys [res] :as res+rec} (p/eval&record-deps {:eval eval-fn
                                                       :root (fs/parent src)
                                                       :path (fs/file-name src)})
        tx (recording->tx src id res+rec)]
    {:type ::prose-doc
     :content res
     :target target
     :tx tx}))


(defmethod build/entity->build-plan ::prose-doc [spec]
  (build spec))


(defmethod build/build! ::prose-doc [conn {:keys [content target  tx]}]
  (tb/write-file {:path target :string content})
  (deref (db/transact conn {:tx-data tx})))



(comment
  (u/with-fresh-temp-ids
    (build {:src "test-resources/prose/includes/main.prose"
            :eval-fn (p/make-evaluator)})))
