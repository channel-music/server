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
  [{:keys [songs player] :as db} [_ song]]
  (when song
    (js/Audio. (str "/uploads" (:file song))))
  (if (:queue player)
    (assoc-in db [:player :status] :playing)
    (assoc db :player {:queue (make-play-queue songs)
                       :status :playing})))

(comment
  (.on audio "ended" #(dispatch! [:songs/next]))
  )

(defmethod handle-event :songs/pause
  [db _]
  (assoc-in db [:player :status] :paused))

(defmethod handle-event :songs/next
  [{:keys [songs player] :as db} _]
  (if-let [pq (next-track (:queue player))]
    (assoc-in db [:player :queue] pq)
    ;; ensure that status is updated when the queue
    ;; is depleted.
    (assoc db :player {:queue nil, :status nil})))

(defmethod handle-event :songs/prev
  [{:keys [songs play-queue] :as db} _]
  (update-in db [:player :queue] previous-track))
