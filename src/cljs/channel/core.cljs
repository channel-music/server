(ns channel.core
  (:require [ajax.core :refer [GET]]
            [channel.db :refer [app-state]]
            [channel.views.core :as views]
            [channel.utils :refer [map->sorted-map]]
            [rum.core :as rum]))

(defn mount! []
  (rum/mount
   (views/main-page app-state)
   (.getElementById js/document "app")))

(defn init! []
  (views/setup-app-routes! app-state)
  (mount!)
  (GET "/api/songs" {:handler #(->> %
                                    (reduce (fn [acc {:keys [id] :as s}]
                                              (assoc acc id s)) {})
                                    (map->sorted-map (juxt :artist :album :track))
                                    (swap! app-state assoc :songs))}))
