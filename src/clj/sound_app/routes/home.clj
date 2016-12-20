(ns sound-app.routes.home
  (:require [sound-app.layout :as layout]
            [sound-app.db.core :as db]
            [compojure.core :refer [defroutes GET POST]]
            [ring.util.response :refer [redirect]]
            [ring.util.http-response :as response]
            [clojure.java.io :as io]
            [claudio.id3 :as id3])
  (:import (java.io File FileInputStream FileOutputStream)))

(defn home-page []
  (layout/render "home.html"))

;; TODO: Move to API
(defn upload-page []
  (layout/render "upload.html"))

;; TODO: Make configurable
(def resource-path "resources/uploads")

(defn upload-file!
  "Store the uploaded temporary file in the directory given my `path`.
  Returns the uploaded file."
  [path {:keys [tempfile size filename]}]
  (let [new-file (io/file path filename)]
    (io/copy tempfile new-file)
    new-file))

(defn save-music-file! [file]
  (let [tag (id3/read-tag file)]
    (db/create-song! {:title  (:title tag)
                      :artist (:artist tag)
                      :album  (:ablum tag)
                      :genre  (:genre tag)
                      ;; TODO: Handle null
                      :track  (Integer/parseUnsignedInt (:track tag))
                      ;; TODO: Store only path relative to resource-path
                      :file   (.getPath file)})
    (redirect "/songs")))

(defn songs-page
  "Displays all uploaded songs."
  []
  (layout/render "songs.html" {:songs (db/all-songs)}))

(defroutes home-routes
  (GET "/" [] (home-page))
  (GET "/songs" [] (songs-page))
  (GET "/upload" [] (upload-page))
  (POST "/upload" [file]
        (->> file
             (upload-file! resource-path)
             (save-music-file!))))
