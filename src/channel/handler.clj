(ns channel.handler
  (:require [compojure.core :refer [routes wrap-routes]]
            [channel.routes.services :refer [service-routes]]
            [compojure.route :as route]
            [channel.env :refer [defaults]]
            [mount.core :as mount]
            [channel.middleware :as middleware]
            [ring.util.http-response :as response]))


(mount/defstate init-app
                :start ((or (:init defaults) identity))
                :stop  ((or (:stop defaults) identity)))


(def app-routes
  (routes
    #'service-routes
    (route/not-found
      (response/not-found {:detail "Not found"}))))


(defn app [] (middleware/wrap-base #'app-routes))
