(ns channel.views.songs
  (:require [channel.events :as events]
            [channel.play-queue :as pq]
            [rum.core :as rum]))

(rum/defc songs-page < rum/reactive
  [db]
  (let [db (rum/react db)
        current-track (pq/track-id (get-in db [:player :queue]))]
    [:.row
     [:.col-lg-12
      [:table.table.table-striped
       [:thead
        [:tr
         [:th {:col-span 1}]
         [:th "#"]
         [:th "Title"]
         [:th "Artist"]
         [:th "Album"]]]
       [:tbody
        (for [s (vals (:songs db)) :let [active? (= (:id s) current-track)]]
          [:tr {:key (:id s), :class (when active? "table-success")}
           [:td [:button {:on-click
                          #(events/dispatch! [:songs/play s])}
                 [:i.fa.fa-play]]]
           [:td (:track s)]
           [:td (:title s)]
           [:td (:artist s)]
           [:td (:album s)]])]]]]))
