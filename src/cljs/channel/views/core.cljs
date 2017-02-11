(ns channel.views.core
  (:require [channel.components :as c]
            [channel.db :refer [app-state]]
            [channel.views.songs :refer [songs-page]]
            [channel.views.upload :refer [upload-page]]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [rum.core :as rum]
            [secretary.core :as secretary :refer-macros [defroute]])
  (:import goog.History))

;; TODO: Find a less DRY way of defining these. Possibly through
;; TODO: a map. Same goes for route definitions below.
(defmulti current-page
  "Returns the component for the currently used page."
  {:default nil}
  #(:page @%))
(defmethod current-page :songs [app-state]
  (songs-page app-state))
(defmethod current-page :upload [app-state]
  (upload-page app-state))
;; TODO: Make 404 page
(defmethod current-page nil [app-state]
  (songs-page app-state))

(rum/defc main-page [app-state]
  [:#wrapper
   (c/sidebar [["Songs" "#/songs"] ["Upload" "#/upload"]])
   [:#page-content-wrapper
    [:.row
     [:.col-lg-12]
     (current-page app-state)]]])

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
