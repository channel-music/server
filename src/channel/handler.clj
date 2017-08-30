(ns channel.handler
  (:require
   [channel.env :refer [defaults]]
   [channel.config :refer [env]]
   [channel.middleware :as middleware]
   [channel.routes.services :refer [service-routes]]
   [compojure.core :refer [routes wrap-routes]]
   [compojure.route :as route]
   [mount.core :as mount]
   [ring.util.http-response :as response]))


(mount/defstate init-app
                :start ((or (:init defaults) identity))
                :stop  ((or (:stop defaults) identity)))


(def app-routes
  (routes
    #'service-routes
    (route/files
     (str "/" (get env :media-url "media"))
     {:root (get env :media-path "media")})
    (route/not-found
      (response/not-found {:detail "Not found"}))))


(defn app [] (middleware/wrap-base #'app-routes))
