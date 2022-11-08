(ns fr.jeremyschoffen.ssg.assets.file
  (:require
    [clojure.tools.build.api :as tb]
    [fr.jeremyschoffen.ssg.build :as build]))



(defn make [src-path dest-path]
  {:type ::asset-file
   :src (str src-path)
   :target (str dest-path)})


(defmethod build/entity->build-plan ::asset-file [spec]
  spec)


(defmethod build/build! ::asset-file [_ spec]
  (tb/copy-file spec))

