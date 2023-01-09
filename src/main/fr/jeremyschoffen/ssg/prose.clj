(ns fr.jeremyschoffen.ssg.prose
  (:require
    [fr.jeremyschoffen.dolly.core :as dolly]
    [fr.jeremyschoffen.prose.alpha.document.common.evaluator :as e]
    [fr.jeremyschoffen.prose.alpha.document.sci :as doc-sci]
    [fr.jeremyschoffen.prose.alpha.eval.common :as eval-common]
    [fr.jeremyschoffen.prose.alpha.eval.sci :as eval-sci]
    [fr.jeremyschoffen.prose.alpha.reader.core :as reader]
    [fr.jeremyschoffen.ssg.prose.utils :as u]
    [medley.core :as medley]))


(defn recording-slurp-doc [path]
  (u/record path)
  (slurp path))



(def default-env
  {:prose.alpha.document/slurp-doc recording-slurp-doc
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


(defn eval&record-deps
  "Evalutate a prose document and record the dependencies.

  Args:
  - `eval`: a function that evaluate a prose document.
  - `eval-env`: The environment for the evaluation.
  - `path`: path of the document to evaluate.

  Result is a map whose keys are
  - `res`: the result of the evaluation.
  - `deps`: paths of the recorded deps.
  "
  [{:keys [eval eval-env path]}]
  (let [recording* (u/make-empty-recording)
        res (u/record-in recording*
              (eval path eval-env))
        recording @recording*]
    {:res res
     :deps recording}))


;; -----------------------------------------------------------------------------
;; Sci stuff
;; -----------------------------------------------------------------------------
(dolly/def-clone doc-sci/make-ns-bindings)

(dolly/def-clone default-sci-nss doc-sci/sci-opt-doc-ns)


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

