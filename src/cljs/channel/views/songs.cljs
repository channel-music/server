(ns channel.views.songs
  (:require [channel.events :as events]
            [rum.core :as rum]))

(rum/defc songs-page < rum/reactive
  [db]
  [:.row
   [:.col-lg-12
    [:table.table.table-striped
     [:thead
      [:tr
       [:th "#"]
       [:th "Title"]
       [:th "Artist"]
       [:th "Album"]
       [:th {:col-span 2}]]]
     [:tbody
      (for [s (vals (:songs (rum/react db)))]
        [:tr
         {:key (:id s)}
         [:th (:track s)]
         [:td (:title s)]
         [:td (:artist s)]
         [:td (:album s)]
         [:td [:button {:on-click
                        #(events/dispatch! [:songs/play s])}
               [:i.fa.fa-play]]]])]]]])
