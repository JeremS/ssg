(ns fr.jeremyschoffen.ssg.assets.prose-doc
  (:require
    [fr.jeremyschoffen.java.nio.alpha.file :as fs]
    [fr.jeremyschoffen.ssg.prose :as p]))


(defn make [src-path dest-path eval-fn & opts]
  {:type :prose-document
   :src src-path
   :target dest-path
   :eval-fn eval-fn})



(defn build! [{:keys [src target eval-fn]:as prose-doc}]
  (let [res (p/eval&record-deps {:eval eval-fn
                                 :root (fs/parent src)
                                 :path (fs/file-name src)})]))
    
