(ns channel.views.songs
  (:require [channel.events :as events]
            [channel.play-queue :as pq]
            [rum.core :as rum]))

(defn current-song  [songs play-queue]
  (->> play-queue pq/track-id (get songs)))

;; TODO: Make a mixin for updating player song progress
;;       *hint* use requestAnimationFrame or something
;; TODO: Define a player progress component

(defn- song-title-display
  "Returns a human readable song title. This joins the title,
  album and artist "
  [{:keys [title album artist]}]
  (clojure.string/join " - " [title album artist]))

(rum/defc audio-player [songs player]
  [:.row
   [:.col-md-3
    [:.btn-group {:role "group"}
     [:button.btn.btn-default {:on-click
                               #(events/dispatch! [:songs/prev])}
      [:i.fa.fa-backward]]
     (if (= (:status player) :playing)
       [:button.btn.btn-default {:on-click
                                 #(events/dispatch! [:songs/pause])}
        [:i.fa.fa-pause]]
       [:button.btn.btn-default {:on-click
                                 #(events/dispatch! [:songs/play])}
        [:i.fa.fa-play]])
     [:butto.btn.btn-default {:on-click
                              #(events/dispatch! [:songs/next])}
      [:i.fa.fa-forward]]]]
   [:.col-md-9
    [:p (song-title-display (current-song songs (:queue player)))]]])

(rum/defc song-list [songs]
  [:table.table.table-striped
   [:thead
    [:tr
     [:th "#"]
     [:th "Title"]
     [:th "Artist"]
     [:th "Album"]
     [:th {:col-span 2}]]]
   [:tbody
    (for [s (sort-by (juxt :artist :album :track) songs)]
      [:tr
       {:key (:id s)}
       [:th (:track s)]
       [:td (:title s)]
       [:td (:artist s)]
       [:td (:album s)]
       [:td [:button {:on-click
                      #(events/dispatch! [:songs/play s])}
             [:i.fa.fa-play]]]])]])

(rum/defc songs-page < rum/reactive
  [db]
  [:div#songs
   [:.row
    [:.col-md-12
     (song-list (vals (:songs (rum/react db))))]]
   [:em (pr-str (:player (rum/react db)))]
   [:.row
    [:.col-md-12
     (let [{:keys [player songs]} (rum/react db)]
       (audio-player songs player))]]])
