(ns sound-app.core)

(defonce current-song (atom nil))

(defn ^:export play-song [elem id]
  (let [audio (.getElementById js/document (str "song-" id))]
    (when (.-paused audio)
      (when-let [[audio elem] @current-song]
        (.pause audio)
        (set! (.-textContent elem) "|>"))
      (set! (.-textContent elem) "||")
      (.play audio)
      (reset! current-song [audio elem]))))

(defn mount-components [])

(defn init! []
  (println "Initializing app..."))
