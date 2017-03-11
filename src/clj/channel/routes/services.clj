(ns channel.routes.services
  (:require [channel.routes.services.auth :as auth]
            [channel.routes.services.songs :as songs]
            [compojure.api.sweet :refer :all]
            [compojure.api.upload :as upload]
            [schema.core :as s]))

(defapi service-routes
  {:swagger {:ui "/swagger-ui"
             :spec "/swagger.json"
             :data {:info {:version "1.0.0"
                           :title "Channel API"
                           :description "API for the Channel web app"}}}}

  (context "/api/auth" []
    :tags ["auth"]

    (POST "/login" req
      :summary "Authenticate user"
      :body-params [username :- s/Str, password :- s/Str]
      :return auth/LoginResponse
      (auth/login username password req)))

  (context "/api/users" []
    ;; :auth-rules admin?
    :tags ["users"]

    (GET "/" []
      :summary "Retrieve all users"
      :return [auth/User]
      (auth/all-users)))

  (context "/api/songs" []
    :tags ["songs"]

    (GET "/" []
      :summary "Retrieve all songs."
      :return [songs/Song]
      (songs/all-songs))

    ;; possible solution is to get the API to request ID3 data first,
    ;; then submit with the full required track data.
    (POST "/" []
      :summary "Create a new song using an MP3 file."
      :description "All song data is extracted from the ID3 metadata of the MP3"
      :multipart-params [file :- upload/TempFileUpload]
      :return songs/Song
      :middleware [upload/wrap-multipart-params]
      (songs/create-song! file))

    (GET "/:id" []
      :summary "Retrieve a specific song."
      :return (s/maybe songs/Song)
      :path-params [id :- Long]
      (songs/get-song id))

    (PUT "/:id" []
      :summary "Update song details."
      :path-params [id :- s/Int]
      :body [song songs/UpdatedSong]
      :return songs/Song
      (songs/update-song! id song))

    (DELETE "/:id" []
      :summary "Delete a specific song."
      :path-params [id :- s/Int]
      :return nil
      (songs/delete-song! id))))
