(ns channel.routes.services
  (:require [channel.routes.services.songs :as songs]
            [compojure.api.sweet :refer :all]
            [compojure.api.upload :as upload]
            [ring.util.http-response :as ring-response]
            [schema.core :as s]))

(defapi service-routes
  {:swagger {:ui "/swagger-ui"
             :spec "/swagger.json"
             :data {:info {:version "1.0.0"
                           :title "Sound file API"
                           :description "API for uploading sound files."}}}}

  (context "/api/songs" []
    :tags ["songs"]

    (GET "/" []
      :return [songs/Song]
      :summary "Retrieve all songs."
      (songs/all-songs))

    ;; possible solution is to get the API to request ID3 data first,
    ;; then submit with the full required track data.
    (POST "/" []
      :return songs/Song
      :multipart-params [file :- upload/TempFileUpload]
      :middleware [upload/wrap-multipart-params]
      :summary "Create a new song using an MP3 file."
      :description "All song data is extracted from the ID3 metadata of the MP3"
      (songs/create-song! file))

    (GET "/:id" []
      :return (s/maybe songs/Song)
      :path-params [id :- Long]
      :summary "Retrieve a specific song."
      (songs/get-song id))

    (PUT "/:id" []
      :return songs/Song
      :path-params [id :- Long]
      :body [song songs/UpdatedSong]
      :summary "Update song details."
      (songs/update-song! id song))

    (DELETE "/:id" []
      :return nil
      :path-params [id :- Long]
      :summary "Delete a specific song."
      (songs/delete-song! id))))
