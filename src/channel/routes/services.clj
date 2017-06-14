(ns channel.routes.services
  (:require
   [channel.media :as media]
   [channel.upload.middleware :as upload.middleware]
   [compojure.api.sweet :refer :all]
   [compojure.api.upload :as api.upload]
   [clojure.java.io :as io]
   [clojure.tools.logging :as log]
   [ring.util.http-response :as response]
   [schema.core :as s]))


(s/defschema Song
  {:title s/Str
   :album s/Str
   :artist s/Str})


(def songs (atom []))


(defn metadata->song [metadata]
  (select-keys metadata [:title :album :artist]))


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
      (let [metadata (media/parse-media-file (:tempfile file))]
        (swap! songs conj (metadata->song metadata))
        (response/created "/songs/1"))
      (catch Exception e
        (io/delete-file (:tempfile file)) ;; cleanup
        (response/bad-request {:detail (.getMessage e), :type (:type (ex-data e))}))))

  (context "/songs" []
    :tags ["songs"]

    (GET "/" []
      :summary "Fetch all songs."
      :return [Song]
      (response/ok @songs))))
