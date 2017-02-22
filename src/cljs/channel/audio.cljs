(ns channel.audio
  "Clojure interface to HTML5 Audio"
  (:require [cljs.core.async :refer [<!] :as async])
  (:require-macros [cljs.core.async.macros :refer [go-loop]]))

;; TODO: Figure out how to "synchronize" browser audio state
;; TODO: and play-queue state OR try and figure out how to provide
;; TODO: a functional-like interface on top of stateful objects.
;;
;; Perhaps this can also be considered a component

(defn- watch-on-ended
  "Calls the function `f` with `audio` once `audio` has ended. Takes a `pause-ms`
  value that represents the amount of time (in ms) to wait before checking the ended
  status again (defaults to 500ms)."
  ([audio f] (watch-on-ended audio f 500))
  ([audio f pause-ms]
   (go-loop []
     (if (.-ended audio)
       (f audio)
       (do
         (<! (async/timeout pause-ms))
         (recur))))))

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
