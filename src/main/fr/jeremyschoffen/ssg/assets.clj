(ns fr.jeremyschoffen.ssg.assets
  (:require
    [fr.jeremyschoffen.ssg.utils :as u]))

(defn make-src-file [path]
  {:type :file
   :asset/path path})

(defn make-production [path deps]
  {:type :file
   :production/path path
   :production/deps (vec deps)})



(defn simple-asset [src-path dest-path]
  (let [temp-id (u/temp-id-maker)
        id (temp-id)]
    [(-> (make-src-file src-path)
         (assoc :db/id id))
     (make-production dest-path [id])]))



(comment
  (simple-asset "toto/titi" "dest/tutu"))


