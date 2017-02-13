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

(defn first-track?
  "Returns true if the play queue `pq` is pointing at the first track."
  [pq]
  (= pq (z/leftmost pq)))

(defn last-track?
  "Returns true if the play queue `pq` is pointing at the last track."
  [pq]
  (= pq (z/rightmost pq)))

(def next-track
  "Returns the play queue shifted one item to the right.
  If there are no more items on the queue, nil is returned."
  z/right)

;; We return nil for `next-track` because it makes sense for the
;; play queue to be depleted once listened to. However, moving too
;; far back in the queue shouldn't remove all tracks.
(defn previous-track
  "Returns the play queue shifted one item to the left.
  If there are no more items to the left, remains on the
  leftmost item."
  [pq]
  (if (first-track? pq)
    pq
    (z/left pq)))

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
