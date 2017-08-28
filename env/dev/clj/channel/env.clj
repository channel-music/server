(ns channel.env
  (:require [clojure.tools.logging :as log]
            [channel.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   #(log/info "\n-=[channel started successfully using the development profile]=-")
   :stop
   #(log/info "\n-=[channel has shut down successfully]=-")
   :middleware wrap-dev})
