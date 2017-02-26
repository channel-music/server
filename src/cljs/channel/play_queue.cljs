(ns channel.play-queue
  (:require [clojure.zip :as z]
            [channel.audio :as audio]
            [channel.events :refer [handle-event dispatch!]]
            [channel.utils :refer [maybe]]))

(defn make-play-queue
  "Creates a new play queue using `songs`."
  [songs]
  (-> (mapv (comp :id second) songs)
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
  (maybe z/node))

;;
;; Handlers
;;
(defn- song->audio
  "Create an audio object using the given `song`, setting up
  all callbacks."
  [song]
  (audio/make-audio song {:on-ended #(dispatch! [:songs/next])}))

(defmethod handle-event :songs/play
  [{:keys [songs player] :as db} [_ song]]
  (if (:queue player)
    (do
      (audio/play!)
      (assoc-in db [:player :status] :playing))
    (do
      (when song
        (-> song
            song->audio
            audio/play!))
      ;; TODO: Reorganize the queue so that the first item is `song`
      (assoc db :player {:queue (make-play-queue songs)
                         :status :playing}))))

(defmethod handle-event :songs/pause
  [db _]
  (audio/pause!)
  (assoc-in db [:player :status] :paused))

(defmethod handle-event :songs/next
  [{:keys [songs player] :as db} _]
  (if-let [pq (next-track (:queue player))]
    (do
      (audio/pause!)
      (audio/play! (-> (get songs (track-id pq))
                       song->audio))
      (update db :player merge {:queue pq, :status :playing}))
    ;; ensure that status is updated when the queue is depleted.
    (assoc db :player {:queue nil, :status nil})))

(defmethod handle-event :songs/prev
  [{:keys [songs player] :as db} [ev-name]]
  (let [pq (previous-track (:queue player))]
    (audio/pause!)
    (audio/play! (-> (get songs (track-id pq))
                     song->audio))
    (update db :player merge {:queue pq, :status :playing})))
