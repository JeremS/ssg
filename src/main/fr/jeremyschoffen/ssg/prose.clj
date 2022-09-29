(ns fr.jeremyschoffen.ssg.prose
  (:require
    [fr.jeremyschoffen.prose.alpha.document.common.evaluator :as e]
    [fr.jeremyschoffen.prose.alpha.document.clojure :as doc]
    [fr.jeremyschoffen.ssg.prose.utils :as u]))


(def default-env
  (assoc doc/default-env :slurp-doc slurp))


(defn make-evaluator [env]
  (let [{:keys [slurp-doc root-dir]
         :as env} (merge default-env env)

        normalize (u/make-path-normalizer root-dir)
        slurp-doc (fn [path]
                    (-> path
                        normalize
                        slurp-doc))]
    (e/make (assoc env :slurp-doc slurp-doc))))


(defn eval&record-deps [{:keys [eval root path]}]
  (let [recording* (u/make-empty-recording)
        res (u/record-in recording*
                           (eval path))
        recording @recording*]
    {:res res
     :deps (-> recording
              (->> (u/recording->graph path))
              (u/normalize-recording root))
     :classification (-> recording
                         u/classify-deps
                         (u/normalize-classification root))}))
