(ns sound-app.core
  (:require [ajax.core :refer [GET POST DELETE]]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [sound-app.components :as c]
            [secretary.core :as secretary :refer-macros [defroute]]
            [reagent.core :as r])
  (:import goog.History))

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

;; FIXME
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
     [:th {:col-span 2}]]]
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

(defn upload-page []
  [:div#upload
   [upload-component]
   [:button.btn.btn-default.btn-primary
    {:on-click #(upload-song!
                 (.getElementById js/document "upload-file"))}
    "Upload"]])

(defn songs-page []
  [songs-component (:songs @app-state)])

(defmulti current-page #(:page @app-state))
(defmethod current-page :songs []
  [songs-page])
(defmethod current-page :upload []
  [upload-page])
;; TODO: Make 404 page
(defmethod current-page :default []
  [songs-page])

(defn main-page []
  [:div#wrapper
   [c/sidebar [["Songs" "#/songs"], ["Upload" "#/upload"]]]
   [:div#page-content-wrapper
    [:div.container-fluid
     #_[c/menu-toggle "Toggle Menu"]
     [:div.row>div.col-lg-12
      [current-page]]]]])

(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
     EventType/NAVIGATE
     (fn [event]
       (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

(defn app-routes []
  (secretary/set-config! :prefix "#")

  (defroute "/" []
    (swap! app-state assoc :page :songs))
  (defroute "/songs" []
    (swap! app-state assoc :page :songs))

  (defroute "/upload" []
    (swap! app-state assoc :page :upload))

  (hook-browser-navigation!))

(defn mount-components []
  (r/render-component
   [main-page]
   (.getElementById js/document "app")))

(defn init! []
  (app-routes)
  (mount-components)
  (GET "/api/songs" {:handler #(swap! app-state assoc :songs (set %))}))
