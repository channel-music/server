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
  ([song {:keys [on-ended]}]
   (let [audio (js/Audio. (:file song))]
     ;; Setup callbacks
     (when on-ended
       (set! (.-onended audio) #(on-ended audio)))
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
   (reset! current-audio audio)
   (.play audio)))
