(ns channel.views.core
  (:require [channel.components :as c]
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
  (fn [page _] page))
(defmethod current-page :songs [_ db]
  (songs-page db))
(defmethod current-page :upload [_ db]
  (upload-page db))
;; TODO: Make 404 page
(defmethod current-page nil [_ db]
  (songs-page db))

(declare songs-path upload-path)

(rum/defc main-page < rum/reactive
  [db]
  [:#wrapper
   (c/sidebar [["Songs" (songs-path)]
               ["Upload" (upload-path)]])
   [:#page-content-wrapper
    [:.row
     [:.col-lg-12
      (current-page
       (:page (rum/react db))
       db)]]]])

(defn- hook-browser-navigation!
  "Hook browser history in to secretary config."
  []
  (doto (History.)
    (events/listen
     EventType/NAVIGATE
     (fn [event]
       (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

(defn setup-app-routes! [db]
  (secretary/set-config! :prefix "#")

  ;; TODO: Use names instead of strings
  (defroute root-path "/" []
    (swap! db assoc :page :songs))
  (defroute songs-path "/songs" []
    (swap! db assoc :page :songs))

  (defroute upload-path "/upload" []
    (swap! db assoc :page :upload))

  (hook-browser-navigation!))
