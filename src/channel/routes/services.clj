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


;; Temporary ID generation
(defn uuid []
  (str (java.util.UUID/randomUUID)))


(s/defschema Song
  {:id     s/Str
   :title  s/Str
   :album  s/Str
   :artist s/Str})


(s/defschema UpdatedSong (dissoc Song :id))


(def songs (atom {}))


(defn metadata->song [metadata]
  (-> metadata
      (select-keys [:title :album :artist])
      (assoc :id (uuid))))


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
            new-song (metadata->song metadata)]
        (swap! songs assoc (:id new-song) new-song)
        (response/created (format "/songs/%s" (:id new-song))))
      (catch Exception e
        (io/delete-file (:tempfile file)) ;; cleanup
        (response/bad-request {:detail (.getMessage e), :type (:type (ex-data e))}))))

  (context "/songs" []
    :tags ["songs"]

    (GET "/" []
      :summary "Fetch all songs."
      :return [Song]
      (response/ok (vals @songs)))

    (context "/:id" []
      (GET "/" [id]
        :summary "Fetch a specific song"
        :return Song
        (if-let [song (get @songs id)]
          (response/ok song)
          (not-found)))

      (PUT "/" [id]
        :summary "Replace the given song's data"
        :body [new-song UpdatedSong]
        :return Song
        (if-let [old-song (get @songs id)]
          (let [new-song (merge old-song new-song)]
            (swap! songs assoc id new-song)
            (response/ok new-song))
          (not-found)))

      (DELETE "/" [id]
        :summary "Remove a specific song"
        :return nil
        (if-let [song (get @songs id)]
          (do
            (swap! songs dissoc id)
            (response/no-content))
          (not-found))))))
