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

;; TODO: Move to API
(defn upload-page []
  (layout/render "upload.html"))

;; TODO: Make configurable
(def resource-path "resources/uploads/")

(defn upload-file!
  "Store the uploaded temporary file in the directory given my `path`.
  Returns the uploaded file."
  [path {:keys [tempfile size filename]}]
  (let [new-file (io/file path filename)]
    (io/copy tempfile new-file)
    new-file))

(defn file->song [file]
  (let [tag (id3/read-tag file)]
    {:title  (:title tag)
     :artist (:artist tag)
     :album  (:ablum tag)
     :genre  (:genre tag)
     :track  (Integer/parseUnsignedInt (:track tag))
     ;; TODO: Store only path relative to resource-path
     :file   (-> file
                 (.getPath)
                 ;; Relative position to resource-path
                 (.replace resource-path ""))}))

(defn validate-unique-song
  "Validates that `song` is unique. Will return `nil` if unique, otherwise
  will return a map containing errors."
  [song]
  (when (:exists (db/song-exists? song))
    {:unique ["Song with that title, artist and album already exists."]}))

(defn create-song! [file]
  (let [song (file->song file)
        ;; the backend ensures that the song is actually unique
        ;; along with the other default validations.
        validator (juxt validate-unique-song
                        v/validate-create-song)]
    (if-let [errors (apply merge (validator song))]
      (log/error "Errors:" errors)
      (do
        (db/create-song! song)
        (redirect "/songs")))))

(defroutes home-routes
  (GET "/" [] (home-page))
  (GET "/upload" [] (upload-page))
  (POST "/upload" [file]
        (->> file
             (upload-file! resource-path)
             (create-song!))))
