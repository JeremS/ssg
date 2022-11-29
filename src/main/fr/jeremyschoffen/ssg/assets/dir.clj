(ns fr.jeremyschoffen.ssg.assets.dir
  (:require
    [fr.jeremyschoffen.ssg.build :as build]))


(defn make [src-path dest-path & {:as opts}]
  {:type ::asset-dir
   :src (str src-path)
   :target (str dest-path)
   :opts (or opts {})})


(defmethod build/entity->build-commands* ::asset-dir
  [{:keys [src target opts] :as spec}]
  (build/copy-dir-cmd spec
                      (merge {:src-dirs [src] :target-dir target} opts)))


