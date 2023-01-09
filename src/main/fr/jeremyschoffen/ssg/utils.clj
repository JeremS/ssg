(ns fr.jeremyschoffen.ssg.utils
  (:require
    [fr.jeremyschoffen.java.nio.alpha.file :as fs]
    [hyperfiddle.rcf :refer [tests]]))


;;-----------------------------------------------------------------------------
;; Path utilities
;;-----------------------------------------------------------------------------
(def dest-root :dest/root)
(def dest-current :dest/current)


(defn add-root [path root]
  (fs/path root path))


(defn remove-root [path root]
  (fs/relativize root path))


(tests
  (def r (fs/path "toto" "tata"))
  (def p (fs/path "titi"))
  (def p-long (fs/path "toto" "tata""titi"))


  (add-root p r) := p-long
  (remove-root p-long r) := p)


(defn ->dest-path [path & {:dest/keys [root current]}]
  (str
    (if (fs/absolute? path)
       (-> path
            (add-root root)
            (remove-root (fs/parent current)))
       path)))


(tests
  (def ex-current "/root/target/pages/section/doc.prose")
  (def ex-root "/root/target/")

  (def ex-path "/assets/pictures/toto.pic")
  (def ex {dest-current ex-current
           dest-root ex-root})



  (->dest-path ex-path ex) :=  "../../assets/pictures/toto.pic")


