(ns fr.jeremyschoffen.ssg.assets.dir
  (:require
    [clojure.tools.build.api :as tb]))


(defn make [src-path dest-path]
  {:type :asset-dir
   :src src-path
   :target dest-path})


(defn build [{:keys [src target]:as asset-dir}]
  (-> asset-dir
      (assoc :src-dirs [src]
             :target-dir target)
      tb/copy-dir))


