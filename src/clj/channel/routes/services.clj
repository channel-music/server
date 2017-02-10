(ns channel.routes.services
  (:require [channel.db.core :as db]
            [channel.validation :as v]
            [channel.songs :as songs]
            [schema.core :as s]
            [compojure.api.sweet :refer :all]
            [compojure.api.upload :as upload]
            [clojure.java.io :as io]
            [ring.util.http-response :as ring-response]))

(s/defschema Song {:id     Long
                   :title  String
                   :artist (s/maybe String)
                   :album  (s/maybe String)
                   :genre  (s/maybe String)
                   :track  s/Int
                   :file   String})
;; Data required to update a song
(s/defschema UpdatedSong (dissoc Song :id :file))

;; TODO: Make configurable
(def resource-path (io/resource "uploads"))

(defn create-song! [file]
  (if-let [errors (v/validate-create-song {:file file})]
    (ring-response/bad-request errors)
    (let [song (songs/create-song! resource-path file)]
      (ring-response/created (str "/api/songs/" (:id song)) song))))

(defn update-song! [old-song new-song]
  (let [song (merge old-song new-song)]
    (if-let [errors (v/validate-update-song song)]
      (ring-response/bad-request errors)
      (-> (songs/update-song! old-song new-song)
          (ring-response/ok)))))

(defn delete-song! [song]
  (songs/delete-song! resource-path song)
  (ring-response/no-content))

(defapi service-routes
  {:swagger {:ui "/swagger-ui"
             :spec "/swagger.json"
             :data {:info {:version "1.0.0"
                           :title "Sound file API"
                           :description "API for uploading sound files."}}}}

  (context "/api" []
    :tags ["songs"]

    (GET "/songs" []
      :return [Song]
      :summary "Retrieve all songs."
      (ring-response/ok (songs/all-songs)))

    ;; possible solution is to get the API to request ID3 data first,
    ;; then submit with the full required track data.
    (POST "/songs" []
      :return Song
      :multipart-params [file :- upload/TempFileUpload]
      :middleware [upload/wrap-multipart-params]
      :summary "Create a new song using an MP3 file."
      :description "All song data is extracted from the ID3 metadata of the MP3"
      (create-song! file))

    (GET "/songs/:id" []
      :return (s/maybe Song)
      :path-params [id :- Long]
      :summary "Retrieve a specific song."
      (if-let [song (songs/song-by-id id)]
        (ring-response/ok song)
        (ring-response/not-found)))

    (PUT "/songs/:id" []
      :return Song
      :path-params [id :- Long]
      :body [new-song UpdatedSong]
      :summary "Update song details."
      (if-let [old-song (db/song-by-id {:id id})]
        (update-song! old-song new-song)
        (ring-response/not-found)))

    (DELETE "/songs/:id" []
      :return nil
      :path-params [id :- Long]
      :summary "Delete a specific song."
      (if-let [song (db/song-by-id {:id id})]
        (delete-song! (db/song-by-id {:id id}))
        (ring-response/not-found)))))
