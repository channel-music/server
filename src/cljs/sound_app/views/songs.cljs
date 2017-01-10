(ns sound-app.views.songs
  (:require [ajax.core :refer [DELETE]]
            [sound-app.db :refer [app-state]]))

(defn delete-song!
  "Request that a song is deleted. Removes from app state on success."
  [song]
  (DELETE (str "/api/songs/" (:id song))
          {:handler #(swap! app-state update :songs disj song)}))

(defn play-song!
  "Play the given song."
  [{:keys [file]}]
  (.play (js/Audio. (str "/uploads/" file))))

(defn songs-component [songs]
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
      ^{:key (:id s)}
      [:tr
       [:th (:track s)]
       [:td (:title s)]
       [:td (:artist s)]
       [:td (:album s)]
       [:td [:button {:on-click #(play-song! s)}
             "Play"]]
       [:td [:button {:on-click #(delete-song! s)}
             "Delete"]]])]])

(defn songs-page []
  [songs-component (:songs @app-state)])
