(ns sound-app.routes.home
  (:require [sound-app.layout :as layout]
            [sound-app.db.core :as db]
            [sound-app.validation :as v]
            [compojure.core :refer [defroutes GET POST]]
            [clojure.tools.logging :as log]
            [clojure.java.io :as io]
            [claudio.id3 :as id3]
            [ring.util.response :refer [redirect]]))

(defn home-page []
  (layout/render "home.html"))

(defroutes home-routes
  (GET "/" [] (home-page)))
