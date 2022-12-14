(ns fr.jeremyschoffen.ssg.assets
  (:require
    [fr.jeremyschoffen.dolly.core :as dolly]
    [fr.jeremyschoffen.ssg.assets.file :as af]
    [fr.jeremyschoffen.ssg.assets.dir :as ad]
    [fr.jeremyschoffen.ssg.assets.prose-doc :as ap]))


(dolly/def-clone asset-file af/make)

(dolly/def-clone asset-dir ad/make)

(dolly/def-clone prose-document ap/make)



(dolly/def-clone get-outdated-asset-files af/get-outdated-files)
(dolly/def-clone get-outdated-prose-docs ap/get-outdated-docs)
