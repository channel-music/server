(ns user
  (:require [mount.core :as mount]
            [sound-app.figwheel :refer [start-fw stop-fw cljs]]
            sound-app.core))

(defn start []
  (mount/start-without #'sound-app.core/http-server
                       #'sound-app.core/repl-server))

(defn stop []
  (mount/stop-except #'sound-app.core/http-server
                     #'sound-app.core/repl-server))

(defn restart []
  (stop)
  (start))


