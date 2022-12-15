(ns fr.jeremyschoffen.ssg.assets.file
  (:require
    [datascript.core :as d]
    [fr.jeremyschoffen.ssg.build :as build]))



(defn make [src-path dest-path]
  {:type ::asset-file
   :src (str src-path)
   :target (str dest-path)})


(defmethod build/entity->build-commands* ::asset-file [{:keys [src target ] :as spec}]
  (build/copy-file-cmd spec :src src :target target))



(defn get-outdated-files [db changed-files]
  (d/q '[:find [(pull ?id [:*]) ...]
         :in $ [?changed-file ...]
         :where
         [?id :type ::asset-file]
         [?id :src ?changed-file]]
       db
       changed-files))
