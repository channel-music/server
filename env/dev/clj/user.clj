(ns user
  (:require [mount.core :as mount]
            channel.core))


(defn start []
  (mount/start-without #'channel.core/repl-server))


(defn stop []
  (mount/stop-except #'channel.core/repl-server))


(defn restart []
  (stop)
  (start))


