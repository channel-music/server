(ns channel.handlers
  (:require [channel.events :refer [reg-event]]
            [channel.play-queue :as pq]))

(reg-event
 :songs/play
 (fn [db [song]]
   (println "Playing song" song)
   (assoc db :play-queue (pq/make-play-queue (:songs db)))))

;; Pauses current playing song
(reg-event
 :songs/pause
 (fn [db]
   (println "Pausing song" (pq/track-id (:play-queue db)))
   db))

(reg-event
 :songs/next
 (fn [db]
   (update db :play-queue pq/next-track)))

(reg-event
 :songs/prev
 (fn [db]
   (update db :play-queue pq/previous-track)))
