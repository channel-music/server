(ns channel.play-queue
  (:require [clojure.zip :as z]
            [channel.events :refer [handle-event]]))

(defn make-play-queue
  "Creates a new play queue using `songs`."
  [songs]
  (-> (mapv :id songs)
      (z/vector-zip)
      ;; Initialize zipper, we only have one level of nesting anyway.
      (z/down)))

(def next-track
  "Returns the play queue shifted one item to the right."
  z/right)
(def previous-track
  "Returns the play queue shifted one item to the left."
  z/left)
(def track-id
  "Returns the ID of the song in the current play queue."
  z/node)

;;
;; Handlers
;;
(defmethod handle-event :songs/play
  [db [_ song]]
  (println "Playing song" song)
  (assoc db :play-queue (make-play-queue (:songs db))))

(defmethod handle-event :songs/pause
  [db _]
  (println "Pausing song" (track-id (:play-queue db)))
  db)

(defmethod handle-event :songs/next
  [db _]
  (update db :play-queue next-track))

(defmethod handle-event :songs/prev
  [db _]
  (update db :play-queue previous-track))
