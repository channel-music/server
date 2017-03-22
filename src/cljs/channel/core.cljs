(ns channel.core
  (:require [ajax.core :refer [GET]]
            [channel.db :refer [app-state]]
            [channel.views.core :as views]
            [rum.core :as rum]))

(defn sorted-map-from-map [keyfn m]
  (let [sorter (fn [k1 k2]
                 (compare (keyfn (get m k1))
                          (keyfn (get m k2))))]
    (into (sorted-map-by sorter) m)))

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
                                    (sorted-map-from-map (juxt :artist :album :track))
                                    (swap! app-state assoc :songs))}))
