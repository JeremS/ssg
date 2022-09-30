(ns fr.jeremyschoffen.ssg.prose
  (:require
    [fr.jeremyschoffen.dolly.core :as dolly]
    [fr.jeremyschoffen.prose.alpha.document.common.evaluator :as e]
    [fr.jeremyschoffen.prose.alpha.document.clojure :as doc]
    [fr.jeremyschoffen.prose.alpha.document.sci :as doc-sci]
    [fr.jeremyschoffen.prose.alpha.eval.sci :as e-sci]
    [fr.jeremyschoffen.ssg.prose.utils :as u]
    [medley.core :as medley]
    [sci.core :as sci]

    fr.jeremyschoffen.ssg.prose.lib
    fr.jeremyschoffen.ssg.prose.utils))

;; -----------------------------------------------------------------------------
;; Generic stuff
;; -----------------------------------------------------------------------------
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


;; -----------------------------------------------------------------------------
;; Sci stuff
;; -----------------------------------------------------------------------------
(dolly/def-clone doc-sci/make-ns-bindings)

(def default-sci-nss
  (medley/deep-merge doc-sci/sci-opt-doc-ns
                      {:namespaces (make-ns-bindings fr.jeremyschoffen.ssg.prose.lib
                                                     fr.jeremyschoffen.ssg.prose.utils)}))

(defn init-sci-ctxt [opts]
  (let [opts (medley/deep-merge default-sci-nss opts)]
    (e-sci/init opts)))

(comment
  (-> (init-sci-ctxt {})
      :env
      deref
      :namespaces
      keys))


(defn make-sci-evaluator [{:keys [sci-opts] :as env}]
  (let [ctxt (init-sci-ctxt sci-opts)
        eval-forms (fn [forms]
                     (let [ctxt (sci/fork ctxt)]
                       (e-sci/eval-forms-in-temp-ns ctxt forms)))]
    (-> env
        (dissoc :sci-opts)
        (assoc :eval-forms eval-forms)
        make-evaluator)))



