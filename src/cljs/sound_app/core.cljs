(ns sound-app.core
  (:require [ajax.core :refer [GET POST DELETE]]
            [reagent.core :as r]))

(defonce app-state (r/atom {}))

(defn delete-song! [song]
  (DELETE (str "/api/songs/" (:id song))
          {:handler #(swap! app-state update :songs disj song)}))

(defn upload-song! [target]
  (let [file (aget (.-files target) 0)
        form-data (doto (js/FormData.)
                    (.append "file" file))]
    (POST "/api/songs" {:body form-data
                        :handler #(swap! app-state update :songs conj %)
                        :error-handler #(println "Failed to upload file:" %)})))

(defn upload-component []
  [:form#upload-form {:enc-type "multipart/form-data"
                      :method "POST"}
   [:label "Upload file:"]
   [:input#upload-file {:type "file"
                        :name "upload-file"}]])

(defn progress-bar [min max value]
  [:div.progress-bar {:role "progressbar"
                      :aria-valuenow value
                      :aria-valuemin min
                      :aria-valuemax max
                      :style {:width "60%"}}
   [:span.sr-only (str value "% Complete")]])

(defn play-song! [{:keys [file]}]
  (.play (js/Audio. (str "/uploads/" file))))

(defn songs-component [songs]
  [:table.table.table-striped
   [:thead
    [:tr
     [:th "#"]
     [:th "Title"]
     [:th "Artist"]
     [:th "Album"]
     [:th {:colspan 2}]]]
   [:tbody
    (for [s (sort-by (juxt :artist :album :track) songs)]
      [:tr {:key (:id s)}
       [:th (:track s)]
       [:td (:title s)]
       [:td (:artist s)]
       [:td (:album s)]
       [:td [:button {:on-click #(play-song! s)}
             "Play"]]
       [:td [:button {:on-click #(delete-song! s)}
             "Delete"]]])]])

(defn home-page []
  [:div
   [:h3 "Sound App"]
   (songs-component (:songs @app-state))
   [:div#upload
    [upload-component]
    [:button.btn.btn-default.btn-primary
     {:on-click #(upload-song!
                  (.getElementById js/document "upload-file"))}
     "Upload"]]])

(defn mount-components []
  (r/render-component
   [home-page]
   (.getElementById js/document "app")))

(defn init! []
  (GET "/api/songs" {:handler #(swap! app-state assoc :songs (set %))})
  (mount-components))
