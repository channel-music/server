(ns sound-app.core)

(defn init! []
  (-> (.getElementById js/document "app")
      (.-innerHTML)
      (set! "Welcome to sound-app")))
