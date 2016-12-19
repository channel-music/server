(ns sound-app.env
  (:require [selmer.parser :as parser]
            [clojure.tools.logging :as log]
            [sound-app.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[sound-app started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[sound-app has shut down successfully]=-"))
   :middleware wrap-dev})
