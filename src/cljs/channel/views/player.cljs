(ns channel.views.player
  (:require [channel.audio :as audio]
            [channel.events :refer [handle-event dispatch!]]
            [channel.play-queue :as pq]
            [rum.core :as rum]))

;;
;; Utils
;;
(defn current-song  [songs play-queue]
  (->> play-queue pq/track-id (get songs)))

(defn song->audio
  "Create an audio object using the given `song`, setting up
  all callbacks."
  [song]
  (let [audio (audio/make-audio song)]
    (.addEventListener audio "ended" #(dispatch! [:songs/next]) true)
    (.addEventListener audio "timeupdate" #(dispatch! [:songs/progress audio]) true)
    audio))

(defn switch-song!
  "Changes the player to a different song by pausing the current
  one, playing the new one and returning the updated player state."
  [player song]
  (audio/pause! (:audio player))
  (let [new-audio (song->audio song)]
    (audio/play! new-audio)
    (merge player {:audio new-audio, :status :playing, :progress 0})))

;
;; Handlers
;;
(defmethod handle-event :songs/play
  [{:keys [songs player] :as db} [_ song]]
  (if (and (not song) (:queue player))
    (do
      (audio/play! (:audio player))
      (assoc-in db [:player :status] :playing))
    (do
      (let [new-player (switch-song! player song)
            pq (-> (pq/make-play-queue songs)
                   (pq/skip-until #(= % (:id song))))]
        ;; Reorganize the queue so that the first item is `song`
        (update db :player merge new-player {:queue pq})))))

(defmethod handle-event :songs/pause
  [db _]
  (audio/pause! (get-in db [:player :audio]))
  (assoc-in db [:player :status] :paused))

(defmethod handle-event :songs/next
  [{:keys [songs player] :as db} _]
  (if-let [pq (pq/next-track (:queue player))]
    (let [new-player (switch-song! player (current-song songs pq))]
      (update db :player merge new-player {:queue pq}))
    ;; ensure that status is updated when the queue is depleted.
    (assoc db :player {:queue nil, :status nil, :audio nil})))

(defmethod handle-event :songs/prev
  [{:keys [songs player] :as db} [ev-name]]
  (let [pq (pq/previous-track (:queue player))
        new-player (switch-song! player (current-song songs pq))]
    (update db :player merge new-player {:queue pq})))

(defmethod handle-event :songs/progress
  [db _]
  (let [progress (audio/progress (get-in db [:player :audio]))]
    (assoc-in db [:player :progress] progress)))

(defmethod handle-event :songs/seek
  [db [_ value]]
  (audio/seek! (get-in db [:player :audio]) value)
  (assoc-in db [:player :progress] value))

;;
;; Views
;;

;; TODO: Make a mixin for updating player song progress
;;       *hint* use requestAnimationFrame or something
;; TODO: Define a player progress component

(rum/defc song-progress
  "Renders a song progress bar. Expects a `value` between 0 and 1."
  [value]
  (let [on-click (fn [ev]
                   (let [rect (-> ev .-currentTarget .getBoundingClientRect)
                         ratio (/ (- (.-clientX ev) (.-x rect)) (.-width rect))]
                     (dispatch! [:songs/seek ratio])))]
    [:progress#seekbar {:value value :max 1 :on-click on-click}]))

(defn- song-title-display
  "Returns a human readable song title. This joins the title,
  album and artist."
  [{:keys [title album artist] :as song}]
  (if song
    (clojure.string/join " - " [title album artist])
    "Not Playing..."))

(rum/defc audio-player [songs player]
  [:.row#player
   (song-progress (:progress player))
   [:.col-md-3
    [:.btn-group {:role "group"}
     [:button.btn.btn-default {:on-click
                               #(dispatch! [:songs/prev])}
      [:i.fa.fa-backward]]
     (if (= (:status player) :playing)
       [:button.btn.btn-default {:on-click
                                 #(dispatch! [:songs/pause])}
        [:i.fa.fa-pause]]
       [:button.btn.btn-default {:on-click
                                 #(dispatch! [:songs/play])}
        [:i.fa.fa-play]])
     [:button.btn.btn-default {:on-click
                               #(dispatch! [:songs/next])}
      [:i.fa.fa-forward]]]]
   [:.col-md-9
    [:p (song-title-display (current-song songs (:queue player)))]]])
