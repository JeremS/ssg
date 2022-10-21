(ns fr.jeremyschoffen.ssg.assets.prose-doc
  (:require
    [asami.core :as db]
    [fr.jeremyschoffen.java.nio.alpha.file :as fs]
    [fr.jeremyschoffen.ssg.build :as build]
    [fr.jeremyschoffen.ssg.prose :as p]
    [fr.jeremyschoffen.ssg.utils :as u]))


(defn make [src-path dest-path eval-fn & opts]
  {:type ::prose-doc
   :src src-path
   :target dest-path
   :eval-fn eval-fn})


(defn make-tx-ids [main-doc-path main-doc-id classification]
  (-> classification
      (->> (reduce-kv
             (fn [acc k v]
               (assoc acc k (u/next-id)))
             {}))
      (assoc main-doc-path main-doc-id)))


(defn recording->deps-entities-tx [classification path->id]
  (into []
        (map (fn [[path t]]
               {:db/id (path->id path)
                :type :prose-dependency
                :prose-dependency-type t
                :path path}))
        classification))


(defn recording->deps-tx [deps-map  path->id]
  (reduce-kv
    (fn [acc f deps]
      (let [id (path->id f)]
        (into acc
              (comp
                (map path->id)
                (map (fn [dep-id]
                       [:db/add id :depends-on dep-id])))
              deps)))
    []
    deps-map))


(defn recording->tx [main-doc-path main-doc-id {:keys [deps classification]}]
  (let [path->id (make-tx-ids main-doc-path main-doc-id classification)]
    (into (recording->deps-entities-tx classification path->id)
          (recording->deps-tx deps  path->id))))


(defn build [{:keys [src target eval-fn]
              :db/keys [id]}]
  (let [{:keys [res deps classification] :as res+rec} (p/eval&record-deps {:eval eval-fn
                                                                           :root (fs/parent src)
                                                                           :path (fs/file-name src)})]
    {:type ::prose-doc
     :content res
     :target target
     :tx (recording->tx src id res+rec)}))


(defmethod build/build ::prose-doc [spec]
  (build spec))


(defmethod build/build! ::prose-doc [conn {:keys [content target compile-fn tx]}]
  (db/transact conn {:tx-data tx})
  (spit target (compile-fn content)))


(comment
  (u/with-fresh-temp-ids
    (build {:src "test-resources/prose/includes/main.prose"
            :eval-fn (p/make-evaluator)})))
