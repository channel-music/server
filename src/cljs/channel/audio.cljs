(ns channel.audio
  "Clojure interface to HTML5 Audio"
  #_(:require [cljs.core.async :as async :include-macros true]))

;; TODO: Figure out how to "synchronize" browser audio state
;; TODO: and play-queue state OR try and figure out how to provide
;; TODO: a functional-like interface on top of stateful objects.
;;
;; Perhaps this can also be considered a component

;; TODO: Use core.async with timeout
(defn- watch-on-ended [audio f]
  ((fn this [_]
     (if (.-ended audio)
       (f audio)
       ;; FIXME: Not the right time to do it
       (js/requestAnimationFrame this)))))

(defn make-audio
  "Create a new `js/Audio` object using song data."
  ([song] (make-audio song nil))
  ([song {:keys [on-ended]}]
   (let [audio (js/Audio. (:file song))]
     ;; Setup callbacks
     (when on-ended
       (watch-on-ended audio on-ended))
     audio)))

(def ^:private current-audio (atom nil))

(defn pause! []
  (when-let [audio @current-audio]
    (.pause audio)))

(defn play!
  ([] (play! @current-audio))
  ([audio]
   (reset! current-audio audio)
   (.play audio)))

(defn muted? []
  (.muted @current-audio))
