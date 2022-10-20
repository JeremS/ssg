(ns fr.jeremyschoffen.ssg.assets.file
  (:require
    [clojure.tools.build.api :as tb]
    [fr.jeremyschoffen.ssg.build :as build]))



(defn make [src-path dest-path]
  {:type ::asset-file
   :src src-path
   :target dest-path})


(defmethod build/build ::asset-file [spec]
  spec)




(defmethod build/build! ::asset-file [spec]
  (tb/copy-file spec))


