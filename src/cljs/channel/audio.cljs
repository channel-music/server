(ns channel.audio
  "Clojure interface to HTML5 Audio")

;; TODO: Figure out how to "synchronize" browser audio state
;; TODO: and play-queue state OR try and figure out how to provide
;; TODO: a functional-like interface on top of stateful objects.

;; TODO: Use core.async
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

(defn play! [audio]
  (.play audio))

(defn pause! [audio]
  (.pause audio))

(defn muted? [audio]
  (.muted audio))
