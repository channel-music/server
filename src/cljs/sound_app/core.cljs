(ns sound-app.core
  (:require [sound-app.db :refer [app-state]]
            [sound-app.views.core :as views]
            [ajax.core :refer [GET]]
            [reagent.core :as r]))

(defn mount-components []
  (r/render-component
   [views/main-page]
   (.getElementById js/document "app")))

(defn init! []
  (views/setup-app-routes!)
  (GET "/api/songs" {:handler #(swap! app-state assoc :songs (set %))}))
