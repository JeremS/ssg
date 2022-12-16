(ns fr.jeremyschoffen.ssg.watch
  (:require
    [missionary.core :as mi]
    [nextjournal.beholder :as beholder])
  (:import
    java.util.concurrent.LinkedBlockingQueue
    java.util.concurrent.TimeUnit))


(defn make-watcher [dir on-change]
  (let [stop (beholder/watch on-change dir)]
    {:dir dir
     :watcher stop}))


(defn stop [watcher]
  (beholder/stop (:watcher watcher)))


(defn on-changes [conn cbs changes]
  (doseq [cb cbs]
    (cb conn changes)))

(comment
  (defonce watcher (beholder/watch prn "test-resources"))
  (beholder/stop watcher)
  (defonce ex (make-watcher "test-resources" prn))
  (stop ex))



(defn make-queue
  {:tag LinkedBlockingQueue}
  []
  (LinkedBlockingQueue.))



(defn poll-p [^java.util.concurrent.LinkedBlockingQueue q timeout unit]
  (mi/sp
    (.poll q timeout unit)))


(defn exec [task & {:keys [on-sucess on-error]
                    :or {on-sucess identity
                         on-error identity}}]
  (task
    on-sucess
    on-error))


(defn stop-process [{:keys [stop]}]
  (stop))


(defn cancel-process [{:keys [cancel]}]
  (cancel))


(defn make-recompiler [queue on-file-change & {:keys [timeout unit on-sucess on-error]
                                               :or {timeout 5 unit TimeUnit/SECONDS}
                                               :as opts}]
  (let [continue? (atom true)
        task (mi/via mi/blk
               (while @continue?
                 (when-let [change (mi/? (poll-p queue timeout unit))]
                   (on-file-change change)))
               :recompiler-done)]
    {:stop #(reset! continue? false)
     :cancel (exec task opts)}))


(comment
  (def queue (make-queue))
  (.put queue (rand-int 30))
  (.put queue :titi)
  (.take queue)
  (.poll queue 1 TimeUnit/SECONDS)

  (def process (make-recompiler queue println :on-error println :on-sucess println))
  (stop-process process)
  (cancel-process process))











