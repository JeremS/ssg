(ns fr.jeremyschoffen.ssg.assets.file
  (:require
    [fr.jeremyschoffen.ssg.build :as build]))



(defn make [src-path dest-path]
  {:type ::asset-file
   :src (str src-path)
   :target (str dest-path)})


(defmethod build/entity->build-commands* ::asset-file [{:keys [src target ] :as spec}]
  (build/copy-file-cmd spec :src src :target target))


