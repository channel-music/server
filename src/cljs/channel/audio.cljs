(ns channel.audio
  "Clojure interface to HTML5 Audio")

;; TODO: Figure out how to "synchronize" browser audio state
;; TODO: and play-queue state OR try and figure out how to provide
;; TODO: a functional-like interface on top of stateful objects.
;;
;; Perhaps this can also be considered a component

(defn make-audio
  "Create a new `js/Audio` object using song data."
  [song]
  (js/Audio. (:file song)))

(def ^:private current-audio (atom nil))

(defn muted? []
  (.muted @current-audio))

(defn pause! []
  (when-let [audio @current-audio]
    (.pause audio)))

(defn play!
  ([] (play! @current-audio))
  ([audio]
   ;; TODO: Consider pausing old audio
   (reset! current-audio audio)
   (.play audio)))

;; FIXME: Working with @current-audio is becomeing unwieldy
(defn progress
  "Returns the current track progress as a value between 0 and 1."
  ([] (progress @current-audio))
  ([audio]
   (/ (.-currentTime audio) (.-duration audio))))

(defn seek!
  "Seek track to a certain point. Expects a `value` between 0 and 1,
  representing the beginning and end of the track."
  ([value] (seek! @current-audio value))
  ([audio value]
   (let [curr-time (* value (.-duration audio))]
     (set! (.-currentTime audio) curr-time))))
