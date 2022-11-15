(ns fr.jeremyschoffen.ssg.assets.dir
  (:require
    [clojure.tools.build.api :as tb]
    [fr.jeremyschoffen.ssg.build :as build]))


(defn make [src-path dest-path & opts]
  {:type ::asset-dir
   :src (str src-path)
   :target (str dest-path)
   :opts opts})


(defmethod build/entity->build-plan ::asset-dir [{:keys [src target opts] :as spec}]
  (merge
    spec
    {:src-dirs [src]
     :target-dir target}
    opts))


(defmethod build/build! ::asset-dir [_ spec]
  (tb/copy-dir spec))

