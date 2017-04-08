(ns channel.views.player
  (:require [channel.audio :as audio]
            [channel.events :refer [handle-event dispatch!]]
            [channel.play-queue :as pq]
            [rum.core :as rum]))

(defn current-song  [songs play-queue]
  (->> play-queue pq/track-id (get songs)))

;;
;; Handlers
;;
(defn- song->audio
  "Create an audio object using the given `song`, setting up
  all callbacks."
  [song]
  (audio/make-audio
   song
   {:on-ended #(dispatch! [:songs/next])
    :on-time-update #(dispatch! [:songs/progress %])}))

(defmethod handle-event :songs/play
  [{:keys [songs player] :as db} [_ song]]
  (if (and (not song) (:queue player))
    (do
      (audio/play!)
      (assoc-in db [:player :status] :playing))
    (do
      ;; FIXME: having 2 tracks playing at the same time should be impossible
      (audio/pause!)
      (-> song song->audio audio/play!)
      ;; Reorganize the queue so that the first item is `song`
      (assoc db :player {:queue (-> (pq/make-play-queue songs)
                                    (pq/skip-until #(= % (:id song))))
                         :status :playing
                         :progress 0}))))

(defmethod handle-event :songs/pause
  [db _]
  (audio/pause!)
  (assoc-in db [:player :status] :paused))

(defmethod handle-event :songs/next
  [{:keys [songs player] :as db} _]
  (if-let [pq (pq/next-track (:queue player))]
    (do
      (audio/pause!)
      (audio/play! (-> (current-song songs pq) song->audio))
      (update db :player merge {:queue pq, :status :playing}))
    ;; ensure that status is updated when the queue is depleted.
    (assoc db :player {:queue nil, :status nil})))

(defmethod handle-event :songs/prev
  [{:keys [songs player] :as db} [ev-name]]
  (let [pq (pq/previous-track (:queue player))]
    (audio/pause!)
    (audio/play! (-> (current-song songs pq) song->audio))
    (update db :player merge {:queue pq, :status :playing})))

(defmethod handle-event :songs/progress
  [db [_ audio]]
  (let [ratio (/ (.-currentTime audio) (.-duration audio))]
    (assoc-in db [:player :progress] ratio)))

;;
;; Views
;;

;; TODO: Make a mixin for updating player song progress
;;       *hint* use requestAnimationFrame or something
;; TODO: Define a player progress component

(defn- song-title-display
  "Returns a human readable song title. This joins the title,
  album and artist."
  [{:keys [title album artist] :as song}]
  (if song
    (clojure.string/join " - " [title album artist])
    "Not Playing..."))

(rum/defc audio-player [songs player]
  [:.row#player
   [:progress {:value (:progress player) :max 1}]
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
