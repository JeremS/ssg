(ns fr.jeremyschoffen.ssg.watch
  (:require
    [missionary.core :as mi]
    [nextjournal.beholder :as beholder]))


(defn make-watcher [dir on-change]
  (let [stop (beholder/watch on-change dir)]
    {:dir dir
     :stop stop}))

(defn stop [watcher]
  (beholder/stop (:stop watcher)))



(comment
  (defonce watcher (beholder/watch prn "test-resources"))
  (beholder/stop watcher)
  (defonce ex (make-watcher "test-resources" prn))
  (stop ex))
