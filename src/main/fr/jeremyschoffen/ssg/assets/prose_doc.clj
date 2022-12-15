(ns fr.jeremyschoffen.ssg.assets.prose-doc
  (:require
    [clojure.data :as data]
    [datascript.core :as d]
    [fr.jeremyschoffen.ssg.db :as db]
    [fr.jeremyschoffen.java.nio.alpha.file :as fs]
    [fr.jeremyschoffen.ssg.build :as build]
    [fr.jeremyschoffen.ssg.prose :as p]))


(defn make [src-path dest-path eval-fn & _]
  (let [s-src (str src-path)]
    {:type ::prose-doc
     :src s-src
     :target (str dest-path)
     :eval-fn eval-fn}))


(defn make-new-dep-tx-data [main-doc-id dep]
  (let [id (db/next-id)]
    [{:db/id id
      :type ::prose-dependency
      :path dep}
     [:db/add main-doc-id :depends-on id]]))


(defn make-remove-dep-tx-data [main-doc-id dep]
  [:db/retract main-doc-id :depends-on dep])


(defn make-data-tx [main-doc-id previous-deps deps]
  (let [[removed-deps new-deps] (data/diff previous-deps deps)
        additions (mapcat (partial make-new-dep-tx-data main-doc-id) new-deps)
        retractions (map (partial make-remove-dep-tx-data main-doc-id) removed-deps)]
    (concat additions retractions)))


(defn recording->tx [{src :src
                      previous-deps :depends-on
                      id :db/id} deps]
  (let [deps (-> deps
                 (->> (into #{} (map str)))
                 (disj src))
        previous-deps (into #{}
                            (comp
                              (filter #(= ::prose-dependency (:type %)))
                              (map :path))
                            previous-deps)]
    (make-data-tx id previous-deps deps)))


(defn build [spec]
  (let [{:keys [src eval-fn]} spec
        {:keys [res deps]} (p/eval&record-deps {:eval eval-fn
                                                :root (fs/parent src)
                                                :path (fs/file-name src)})]
    {:document res
     :tx (recording->tx spec deps)}))


(defmethod build/entity->build-commands* ::prose-doc [spec]
  (let [{:keys [target]} spec
        {:keys [document tx]} (build spec)]
    (build/composite-cmd spec
      (build/write-file-cmd spec :path target :string document)
      (build/transact-cmd spec :tx-data tx))))


(def rules
  '[[(main-doc-change ?id ?changed-file)
     [?id :src ?changed-file]]
    [(dependency-change ?id ?changed-file)
     [?id :depends-on ?dep]
     [?dep :path ?changed-file]]])


(defn get-outdated-docs [db changed-files]
  (d/q '[:find [(pull ?id [:*]) ...]
         :in $ % [?changed-file ...]
         :where
         [?id :type ::prose-doc]
         (or
           (main-doc-change ?id ?changed-file)
           (dependency-change ?id ?changed-file))]
       db
       rules
       changed-files))
