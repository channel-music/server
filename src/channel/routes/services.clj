(ns channel.routes.services
  (:require
   [channel.db.core :refer [*db*]]
   [channel.db.songs :as db.songs]
   [channel.io :as cio]
   [channel.media :as media]
   [channel.storage :as storage :refer [*storage*]]
   [channel.upload.middleware :as upload.middleware]
   [compojure.api.sweet :refer :all]
   [compojure.api.upload :as api.upload]
   [clojure.java.io :as io]
   [clojure.tools.logging :as log]
   [ring.util.http-response :as response]
   [schema.core :as s]))


(s/defschema Song
  {:id     s/Int
   :title  s/Str
   :album  s/Str
   :artist s/Str
   :genre  s/Str
   :track  (s/maybe s/Int)
   :file    s/Str})


(s/defschema UpdatedSong (dissoc Song :id :file))


(defn store-file!
  "Store file in storage, generating a unique filename for it. Will
  retry the operation on UUID collision."
  [file]
  (try
    (let [filename (storage/generate-filename file)]
      (storage/store! *storage* file filename))
    (catch clojure.lang.ExceptionInfo e
      (case (:type (ex-data e))
        ;; UUID collision, very rare
        :duplicate-object (store-file! file)
        (throw e)))))


(defn make-song
  "Create a new song, store its related `file` in storage and commit
  to the database. Returns the song data if successful and will throw
  an error if the song already exists."
  [metadata file]
  (let [song (assoc metadata :file (store-file! file))]
    (if (db.songs/song-exists? *db* song)
      (throw (ex-info "Song already exists" {:type :duplicate-record}))
      (merge song (db.songs/create-song! *db* song)))))


(defn not-found
  "Sets request status to 404 and adds a default message."
  []
  (response/not-found {:detail "Not found"}))


(defapi service-routes
  {:swagger {:ui "/swagger-ui"
             :spec "/swagger.json"
             :data {:info {:version (System/getProperty "channel.version")
                           :title "Channel API"
                           :description "API for the Channel service"}}}}

  (POST "/upload" []
    :summary "Upload a new song file"
    :multipart-params [file :- api.upload/TempFileUpload]
    :middleware [upload.middleware/wrap-multipart-params]
    (try
      (let [metadata (media/parse-media-file (:tempfile file))
            new-song (make-song metadata (:tempfile file))]
        (response/created (format "/songs/%d" (:id new-song))))
      (catch clojure.lang.ExceptionInfo e
        (io/delete-file (:tempfile file)) ;; cleanup
        (response/bad-request {:detail (.getMessage e), :type (:type (ex-data e))}))))

  (context "/songs" []
    :tags ["songs"]

    (GET "/" []
      :summary "Fetch all songs."
      :return [Song]
      (response/ok (db.songs/all-songs *db*)))

    (context "/:id" []
      (GET "/" []
        :path-params [id :- Long]
        :summary "Fetch a specific song"
        :return Song
        (if-let [song (db.songs/song-by-id *db* {:id id})]
          (response/ok song)
          (not-found)))

      (PUT "/" []
        :path-params [id :- Long]
        :summary "Replace the given song's data"
        :body [new-song UpdatedSong]
        :return Song
        (if-let [old-song (db.songs/song-by-id *db* {:id id})]
          (let [new-song (merge old-song new-song)]
            (db.songs/update-song! *db* new-song)
            (response/ok new-song))
          (not-found)))

      (DELETE "/" []
        :path-params [id :- Long]
        :summary "Remove a specific song"
        :return nil
        (if-let [song (db.songs/song-by-id *db* {:id id})]
          (do
            (db.songs/delete-song! *db* {:id id})
            (response/no-content))
          (not-found))))))
