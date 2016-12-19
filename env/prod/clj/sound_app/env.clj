(ns sound-app.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[sound-app started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[sound-app has shut down successfully]=-"))
   :middleware identity})
