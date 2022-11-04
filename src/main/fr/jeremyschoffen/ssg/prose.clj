(ns fr.jeremyschoffen.ssg.prose
  (:require
    [fr.jeremyschoffen.dolly.core :as dolly]
    [fr.jeremyschoffen.prose.alpha.document.common.evaluator :as e]
    [fr.jeremyschoffen.prose.alpha.document.sci :as doc-sci]
    [fr.jeremyschoffen.prose.alpha.eval.common :as eval-common]
    [fr.jeremyschoffen.prose.alpha.eval.sci :as eval-sci]
    [fr.jeremyschoffen.prose.alpha.reader.core :as reader]
    [fr.jeremyschoffen.ssg.prose.utils :as u]
    [medley.core :as medley]

    fr.jeremyschoffen.ssg.prose.lib))


(defn slurp-doc [path]
  (let [root (-> (eval-common/get-env) ::root-dir)
        path' (cond->> path
                root (u/normalize-path root))]
    (slurp path')))


(def default-env
  {:prose.alpha.document/slurp-doc slurp-doc
   :prose.alpha.document/read-doc reader/read-from-string
   :prose.alpha.document/eval-forms eval-common/eval-forms-in-temp-ns})


;; -----------------------------------------------------------------------------
;; Generic stuff
;; -----------------------------------------------------------------------------

(defn make-evaluator [& {:as env}]
  (let [env (merge default-env env)]
    (fn eval-doc
      ([path]
       (eval-doc path {}))
      ([path opts]
       (-> env
           (assoc :prose.alpha.document/path path)
           (merge  opts)
           e/eval-doc)))))


(defn eval&record-deps [{:keys [eval root path]}]
  (let [recording* (u/make-empty-recording)
        res (u/record-in recording*
                           (eval path {::root-dir root}))
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
    (eval-sci/init opts)))


(defn make-sci-evaluator [& {:keys [sci-opts] :as env}]
  (let [ctxt (init-sci-ctxt sci-opts)
        eval-forms (fn [forms]
                     (let [ctxt (eval-sci/fork-sci-ctxt ctxt)]
                       (eval-sci/eval-forms ctxt forms)))]
    (-> env
        (dissoc :sci-opts)
        (assoc :prose.alpha.document/eval-forms eval-forms)
        make-evaluator)))



