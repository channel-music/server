(ns channel.views.songs
  (:require [ajax.core :refer [DELETE]]
            [channel.db :refer [app-state]]
            [clojure.zip :as z]
            [rum.core :as rum]))

(defn delete-song!
  "Request that a song is deleted. Removes from app state on success."
  [song]
  (DELETE (str "/api/songs/" (:id song))
          {:handler #(swap! app-state update :songs disj song)}))

(defn current-song [queue]
  (and queue (z/node queue)))

(defn play-song!
  "Play the given song."
  [{:keys [file] :as song}]
  (if-let [queue (:play-queue @app-state)]
    (->> file
         (str "/uploads/")
         (js/Audio.)
         (.play))
    ;; Create fresh play queue
    (let [{:keys [songs]} @app-state]
      ;; FIXME: Handle case where play-queue is still nil
      (swap! app-state assoc :play-queue (-> songs
                                             (z/vector-zip)
                                             (z/down)))
      (play-song! song))))

(rum/defc audio-player []
  (let [queue (:play-queue @app-state)]
    [:div#audio-player
     #_[:div#info
        [:p (:title (current-song queue))]]
     [:div#controls
      [:button#prev {:on-click #(play-song! (-> (z/right queue)
                                                (z/node)))}
       [:i.fa.fa-backward]]
      [:button#play {:on-click #(play-song! (or (current-song queue)
                                                ;; Pick a random song if there isn't one
                                                (rand-nth (vec (:songs @app-state)))))}
       [:i.fa.fa-play]]
      [:button#next {:on-click #(play-song! (-> (z/left queue)
                                                (z/node)))}
       [:i.fa.fa-forward]]]]))

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
       [:td [:button {:on-click #(play-song! s)}
             [:i.fa.fa-play]]]
       [:td [:button {:on-click #(delete-song! s)}
             [:i.fa.fa-trash]]]])]])

(rum/defc songs-page < rum/reactive
  [db]
  [:div#songs
   (song-list (:songs (rum/react app-state)))
   (audio-player)])
