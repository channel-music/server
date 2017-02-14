(ns channel.views.songs
  (:require [channel.events :as events]
            [channel.play-queue]
            [rum.core :as rum]))

(rum/defc audio-player [player]
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
    [:p "Song title"]]])

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
     (song-list (:songs (rum/react db)))]]
   [:em (pr-str (:player (rum/react db)))]
   [:.row
    [:.col-md-12
     (audio-player (:player (rum/react db)))]]])
