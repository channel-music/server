(ns sound-app.routes.services
  (:require [sound-app.db.core :as db]
            [sound-app.validation :as v]
            [schema.core :as s]
            [compojure.api.sweet :refer :all]
            [clojure.java.io :as io]
            [claudio.id3 :as id3]
            [ring.util.http-response :refer :all]))

(s/defschema Song {:id     Long
                   :title  String
                   :artist (s/maybe String)
                   :album  (s/maybe String)
                   :genre  (s/maybe String)
                   :track  s/Int
                   :file   String})

(s/defschema UpdatedSong (dissoc Song :id :file))

(defn upload-file!
  "Store the uploaded temporary file in the directory given my `path`.
  Returns the uploaded file."
  [path {:keys [tempfile size filename]}]
  (let [new-file (io/file path filename)]
    (io/copy tempfile new-file)
    new-file))

;; TODO: Make configurable
(def resource-path "resources/uploads/")

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
      errors
      (do
        (db/create-song! song)
        song))))

(defapi service-routes
  {:swagger {:ui "/swagger-ui"
             :spec "/swagger.json"
             :data {:info {:version "1.0.0"
                           :title "Sample API"
                           :description "Sample Services"}}}}

  (context "/api" []
           :tags ["base"]

           (GET "/songs" []
                :return [Song]
                (ok (db/all-songs)))

           ;; possible solution is to get the API to request ID3 data first,
           ;; then submit with the full required track data.
           (POST "/songs" []
                 :return Song
                 :body [file :- String]
                 :summary "Create a new song using an MP3 file."
                 :description "All song data is extracted from the ID3 metadata of the MP3"
                 (ok (-> file
                         (upload-file! resource-path)
                         (create-song!))))

           (PUT "/songs/:id" []
                :return Song
                :path-params [id :- Long]
                :body [song UpdatedSong]
                :summary "Update song details."
                (ok (db/song-by-id {:id id})))))
