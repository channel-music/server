(ns channel.play-queue
  (:require [clojure.zip :as z]))

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
