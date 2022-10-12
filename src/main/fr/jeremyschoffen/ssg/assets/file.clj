(ns fr.jeremyschoffen.ssg.assets.file
  (:require
    [clojure.tools.build.api :as tb]))


(defn make [src-path dest-path]
  {:type :asset-file
   :src src-path
   :target dest-path})


(defn build [asset]
  (tb/copy-file asset))
