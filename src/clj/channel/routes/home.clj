(ns channel.routes.home
  (:require [channel.layout :as layout]
            [compojure.core :refer [defroutes GET]]))

(defn home-page []
  (layout/render "home.html"))

(defroutes home-routes
  (GET "/" [] (home-page)))
