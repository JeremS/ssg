(ns fr.jeremyschoffen.ssg.assets.dir
  (:require
    [clojure.tools.build.api :as tb]
    [fr.jeremyschoffen.ssg.build :as build]))


(defn make [src-path dest-path & opts]
  {:type :asset-dir
   :src src-path
   :target dest-path
   :opts opts})


(defn build [{:keys [src target opts]}]
  (merge
    {:type ::asset-dir
     :src-dirs [src]
     :target-dir target}
    opts))


(defmethod build/build! ::asset-dir [_ spec]
  (tb/copy-dir spec))


