(ns channel.routes.services.songs
  "Functions that wrap operations in `channel.songs` to be
  used as part of an API."
  (:require [channel.songs :as songs]
            [channel.validation :as v]
            [schema.core :as s]
            [ring.util.http-response :as ring-response]
            [channel.db.core :as db]))

(s/defschema Song {:id     s/Int
                   :title  s/Str
                   :artist (s/maybe s/Str)
                   :album  (s/maybe s/Str)
                   :genre  (s/maybe s/Str)
                   :track  s/Int
                   :file   s/Str})
;; Data required to update a song
(s/defschema UpdatedSong (dissoc Song :id :file))

(defn all-songs []
  (ring-response/ok (songs/all-songs)))

(defn get-song [id]
  (if-let [song (songs/song-by-id id)]
    (ring-response/ok song)
    (ring-response/not-found)))

(defn create-song! [file]
  (if-let [errors (v/validate-create-song {:file file})]
    (ring-response/bad-request errors)
    (let [song (songs/create-song! file)]
      (ring-response/created (str "/api/songs/" (:id song)) song))))

(defn update-song! [id new-song]
  (if-let [old-song (db/song-by-id {:id id})]
    (let [song (merge old-song new-song)]
      (if-let [errors (v/validate-update-song song)]
        (ring-response/bad-request errors)
        (-> (songs/update-song! old-song new-song)
            (ring-response/ok))))
    (ring-response/not-found)))

(defn delete-song! [id]
  (if-let [song (db/song-by-id {:id id})]
    (do
      (songs/delete-song! song)
      (ring-response/no-content))
    (ring-response/not-found)))
