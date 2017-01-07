(ns sound-app.core
  (:require [reagent.core :as r]))

(defonce app-state (r/atom {}))

(defn home-page []
  [:h3 "Welcome to Sound App"])

(defn mount-components []
  (r/render-component
   [home-page]
   (.getElementById js/document "app")))

(defn init! []
  (mount-components))
