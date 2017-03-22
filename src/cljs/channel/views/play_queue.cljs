(ns channel.views.play-queue
  (:require [channel.play-queue :as pq]
            [rum.core :as rum]))

(rum/defc play-queue-page < rum/reactive
  [db]
  (let [queue (get-in (rum/react db) [:player :queue])
        songs (:songs (rum/react db))]
    [:.row
     [:.col-lg-12
      [:table.table.table-stripped
       ;; FIXME: reduce duplication with songs-page
       [:thead
        [:tr
         [:th "#"]
         [:th "Title"]
         [:th "Artist"]]]
       [:tbody
        (when queue
          (for [id (pq/remaining-tracks queue) :let [s (get songs id)]]
            [:tr
             {:key id}
             [:th (:track s)]
             [:td (:title s)]
             [:td (:artist s)]
             [:td (:album s)]]))]]]]))
