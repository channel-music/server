(ns channel.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[channel started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[channel has shut down successfully]=-"))
   :middleware identity})
