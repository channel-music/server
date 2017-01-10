(ns sound-app.views.core
  (:require [sound-app.components :as c]
            [sound-app.db :refer [app-state]]
            [sound-app.views.upload :refer [upload-page]]
            [sound-app.views.songs :refer [songs-page]]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [secretary.core :as secretary :refer-macros [defroute]]
            [reagent.core :as r])
  (:import goog.History))

;; TODO: Find a less DRY way of defining these. Possibly through
;; TODO: a map. Same goes for route definitions below.
(defmulti current-page
  "Returns the component for the currently used page."
  #(:page @app-state))
(defmethod current-page :songs []
  [songs-page])
(defmethod current-page :upload []
  [upload-page])
;; TODO: Make 404 page
(defmethod current-page :default []
  [songs-page])

(defn main-page
  "Main view page, all other pages are rendered as children of this one."
  []
  [:div#wrapper
   [c/sidebar [["Songs" "#/songs"], ["Upload" "#/upload"]]]
   [:div#page-content-wrapper
    [:div.container-fluid
     #_[c/menu-toggle "Toggle Menu"]
     [:div.row>div.col-lg-12
      [current-page]]]]])

(defn- hook-browser-navigation!
  "Hook browser history in to secretary config."
  []
  (doto (History.)
    (events/listen
     EventType/NAVIGATE
     (fn [event]
       (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

(defn setup-app-routes! []
  (secretary/set-config! :prefix "#")

  ;; TODO: Use names instead of strings
  (defroute root-path "/" []
    (swap! app-state assoc :page :songs))
  (defroute songs-path "/songs" []
    (swap! app-state assoc :page :songs))

  (defroute upload-path "/upload" []
    (swap! app-state assoc :page :upload))

  (hook-browser-navigation!))
