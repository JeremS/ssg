(ns fr.jeremyschoffen.ssg.prose.lib
  (:require
    [fr.jeremyschoffen.prose.alpha.document.lib :as lib]
    [fr.jeremyschoffen.ssg.prose.utils :as u]))


(defmacro insert-doc [path]
  `(do
     (u/enter-deps ~path :insert)
     (let [res# (lib/insert-doc ~path)]
       (u/leave-deps ~path)
       res#)))


(defn require-doc [path]
  (u/enter-deps path :require)
  (let [res (lib/require-doc path)]
    (u/leave-deps path)
    res))



