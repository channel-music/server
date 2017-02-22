(ns channel.songs
  (:require [channel.config :refer [env]]
            [channel.db.core :as db]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [mount.core :refer [defstate]])
  (:import com.mpatric.mp3agic.Mp3File))

;; Notes:
;; - Consider that the songs module could be a stateful component
;;   in itself.
;; - Perhaps find a better way to store uploads-path. In some way, the handlers
;;   should be responsible for setting the upload-dir. In another way, it reduces
;;   the number of arguments to pass to the create-song! and delete-song! functions.
;;   One must also consider that the uploads-path is a mostly static property once
;;   defined (as in it doesn't change at runtime).

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
    0)) ;; FIXME: nil should be allowed for tracks

;; Can't define it as a normal variable because it depends
;; on env, which is a component.
(defstate uploads-path :start (env :uploads-path))

;; FIXME: should return a nested filepath :artist/:album/:title
(defn- song->filename
  [{:keys [title album artist]}]
  (let [str->filename #(-> % str/lower-case (str/replace #"\s+" "-"))]
    (->> [artist album title]
         (map str->filename)
         (str/join "-"))))

(defn- save-file!
  [song tempfile]
  (let [filename (str (song->filename song) ".mp3")
        new-file (io/file uploads-path filename)]
    (io/copy tempfile new-file)
    ;; We can assume mp3 for now, validatios catch this
    ;;
    ;; FIXME: Find a proper way to generate relative URL's instead of using
    ;; the java File Object
    (.toString (io/file "/uploads" filename))))

(defn file->song
  "Extracts song data from the file. Returns `nil` on read failure."
  [file]
  (when-let [tag (parse-id3 file)]
    {:title  (.getTitle tag)
     :artist (.getArtist tag)
     :album  (.getAlbum tag)
     :genre  (.getGenre tag)
     :track  (track-number (.getTrack tag))}))

(def all-songs db/all-songs)

(defn song-by-id [id]
  (db/song-by-id {:id id}))

(defn create-song!
  "Attempt to create a new song using `file`. Returns the `song`
  if it already exists."
  [{:keys [tempfile]}]
  (let [song (file->song tempfile)]
    (if-let [existing-song (db/song-exists? song)]
      existing-song
      ;; Ovewrite song giving the file path
      (let [song (->> (save-file! song tempfile)
                      (assoc song :file))]
        (merge song (db/create-song<! song))))))

(defn update-song! [old-song new-song]
  (-> (merge old-song new-song)
      (db/update-song!)))

(defn delete-song! [song]
  (io/delete-file (io/file uploads-path (:file song)))
  (db/delete-song! song))
