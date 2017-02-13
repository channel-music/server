(ns channel.play-queue
  (:require [clojure.zip :as z]
            [channel.events :refer [handle-event]]))

(defn make-play-queue
  "Creates a new play queue using `songs`."
  [songs]
  (-> (mapv :id songs)
      (z/vector-zip)))

(def next-track
  "Returns the play queue shifted one item to the right."
  z/next)
(def previous-track
  "Returns the play queue shifted one item to the left."
  z/prev)
(def track-id
  "Returns the ID of the song in the current play queue."
  z/node)

;;
;; Handlers
;;
(defmethod handle-event :songs/play
  [_ db song]
  (println "Playing song" song)
  (assoc db :play-queue (make-play-queue (:songs db))))

;; Pauses current playing song
(defmethod handle-event :songs/pause
  [_ db]
  (println "Pausing song" (track-id (:play-queue db)))
  db)

(defmethod handle-event :songs/next
  [_ db]
  (update db :play-queue next-track))

(defmethod handle-event :songs/prev
  [_ db]
  (update db :play-queue previous-track))
