(ns channel.audio
  "Clojure interface to HTML5 Audio")

;; TODO: Figure out how to "synchronize" browser audio state
;; TODO: and play-queue state OR try and figure out how to provide
;; TODO: a functional-like interface on top of stateful objects.
;;
;; Perhaps this can also be considered a component

(defn make-audio
  "Create a new `js/Audio` object using song data."
  ([song] (make-audio song nil))
  ([song {:keys [on-ended on-time-update]}]
   (let [audio (js/Audio. (:file song))]
     ;; Setup callbacks
     ;; TODO: Consider making generic
     (when on-ended
       (.addEventListener audio "ended" #(on-ended audio) true))
     (when on-time-update
       (.addEventListener audio "timeupdate"
                          #(on-time-update audio) true))
     audio)))

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
