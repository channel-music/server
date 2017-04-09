(ns channel.audio
  "Clojure interface to HTML5 Audio")

(defn make-audio
  "Create a new `js/Audio` object using song data."
  [song]
  (js/Audio. (:file song)))

(defn muted? [audio]
  (.muted audio))

(defn pause! [audio]
  (when audio
    (.pause audio)))

(defn play!
  [audio]
  (when audio
    (.play audio)))

(defn progress
  "Returns the current track progress as a value between 0 and 1."
  [audio]
  {:post [(<= 0 % 1)]}
  (if (js/isNaN (.-duration audio))
    0
    (/ (.-currentTime audio) (.-duration audio))))

(defn seek!
  "Seek track to a certain point. Expects a `value` between 0 and 1,
  representing the beginning and end of the track."
  [audio value]
  {:pre [(<= 0 value 1)]}
  (let [curr-time (* value (.-duration audio))]
    (set! (.-currentTime audio) curr-time)))
