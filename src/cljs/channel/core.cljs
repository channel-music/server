(ns channel.core
  (:require [channel.db :refer [app-state]]
            [channel.views.core :as views]
            [ajax.core :refer [GET]]
            [reagent.core :as r]))

(defn mount-components []
  (r/render-component
   [views/main-page]
   (.getElementById js/document "app")))

(defn init! []
  (views/setup-app-routes!)
  (mount-components)
  (GET "/api/songs" {:handler #(swap! app-state assoc :songs %)}))
