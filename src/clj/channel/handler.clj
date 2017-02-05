(ns channel.handler
  (:require [compojure.core :refer [routes wrap-routes]]
            [channel.layout :refer [error-page]]
            [channel.routes.home :refer [home-routes]]
            [channel.routes.services :refer [service-routes]]
            [compojure.route :as route]
            [channel.env :refer [defaults]]
            [mount.core :as mount]
            [channel.middleware :as middleware]))

(mount/defstate init-app
                :start ((or (:init defaults) identity))
                :stop  ((or (:stop defaults) identity)))

(def app-routes
  (routes
    (-> #'home-routes
        (wrap-routes middleware/wrap-csrf)
        (wrap-routes middleware/wrap-formats))
    #'service-routes
    (route/resources "/" {:root "public"}) ;; serve static files
    (route/resources "/uploads" {:root "uploads"})
    (route/not-found
      (:body
        (error-page {:status 404
                     :title "page not found"})))))


(defn app [] (middleware/wrap-base #'app-routes))
