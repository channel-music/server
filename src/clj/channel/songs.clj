(ns channel.songs
  (:require [channel.db.core :as db]
            [clojure.java.io :as io])
  (:import (com.mpatric.mp3agic Mp3File)))

(defn- parse-id3
  "Takes a file and attempts to parse MP3 ID3 data from it."
  [file]
  ;; TODO: support other versions
  (.getId3v2Tag (Mp3File. file)))

(defn- track-number
  "Take a string representing a track ratio (E.g. 3/12) and return
  a pair of [num denom] (E.g. [3 12])"
  [ratio-str]
  (if ratio-str
    (-> (clojure.string/split ratio-str #"/")
        (first)
        ;; TODO: Handle invalid int
        (Integer/parseUnsignedInt))
    0))

(defn file->song
  "Extracts song data from the file. Returns `nil` on read failure."
  [file]
  (when-let [tag (parse-id3 file)]
    {:title  (.getTitle tag)
     :artist (.getArtist tag)
     :album  (.getAlbum tag)
     :genre  (.getGenre tag)
     :track  (track-number (.getTrack tag))}))

(defn- save-file!
  "Store the uploaded temporary file in the directory given my `path`.
  Returns the uploaded file."
  [path {:keys [tempfile filename]}]
  (let [new-file (io/file path filename)]
    (io/copy tempfile new-file)
    new-file))

(def all-songs db/all-songs)

(defn song-by-id [id]
  (db/song-by-id {:id id}))

(defn create-song!
  "Attempt to create a new song using `file`. Returns the `song`
  if it already exists."
  [resource-path file]
  (let [song (file->song (:tempfile file))]
    (if-let [existing-song (db/song-exists? song)]
      existing-song
      ;; FIXME: Not serving a file path, I'm serving a URI
      (let [file-path (-> (save-file! resource-path file)
                          (.getPath)
                          ;; FIXME: This makes assuptions about `resource-path`
                          (.replace (.getPath resource-path) "/uploads"))]
        (->> file-path
             (assoc song :file)
             db/create-song<!
             (merge song))))))

(defn update-song! [old-song new-song]
  (-> (merge old-song new-song)
      (db/update-song!)))

(defn delete-song! [resource-path song]
  (io/delete-file (io/file resource-path (:file song)))
  (db/delete-song! song))
