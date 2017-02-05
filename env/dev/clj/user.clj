(ns user
  (:require [mount.core :as mount]
            [channel.figwheel :refer [start-fw stop-fw cljs]]
            channel.core))

(defn start []
  (mount/start-without ;; #'channel.core/http-server
                       #'channel.core/repl-server))

(defn stop []
  (mount/stop-except ;; #'channel.core/http-server
                     #'channel.core/repl-server))

(defn restart []
  (stop)
  (start))


