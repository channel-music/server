(ns sound-app.core
  (:require [rum.core :as rum]))

(rum/defcs time-label < {:did-mount (fn [state]
                                      (assoc state ::time (js/Date.)))}
  [state label]
  [:div label ": " (str (::time state))])

(defn mount-components []
  (rum/mount
   (time-label "The mount time is")
   (.getElementById js/document "app")))

(defn init! []
  (mount-components))
