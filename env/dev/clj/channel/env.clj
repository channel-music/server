(ns channel.env
  (:require [selmer.parser :as parser]
            [clojure.tools.logging :as log]
            [channel.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[channel started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[channel has shut down successfully]=-"))
   :middleware wrap-dev})
