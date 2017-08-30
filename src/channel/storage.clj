(ns channel.storage
  (:require
   [channel.config :refer [env]]
   [channel.io :refer [str->path file-extension]]
   [clojure.java.io :as io]
   [clojure.string :as string]
   [mount.core :refer [defstate]])
  (:import (java.io FileNotFoundException IOException)))


(defprotocol Storable
  "Represents a way of persistently storing objects."

  (store! [this in-stream name]
    "Copy a stream to storage, using `name` as the unique identifier.
Returns the path relative to the storage location. Throws an exception
if the file already exists.")

  (retrieve! [this name]
    "Fetch a stream from storage using its name.
Returns `nil` if the object does not exist.")

  (dispose! [this name]
    "Remove object from storage using its name.
Returns `true` on success and `false` otherwise."))


(defn path-relative-to-root
  "Returns the final file path relative to the provided `root` path."
  [root relative]
  (if (string/starts-with? relative root)
    (str (.relativize (str->path root) (str->path relative)))
    (throw (ex-info
            "Can't give a path relative to a root that doesn't exist"
            {:root root, :relative relative}))))


(defn generate-filename
  "Generate a filename for the given `file`, preserving the extension."
  [file]
  (let [uuid (java.util.UUID/randomUUID)]
    (if-let [ext (file-extension file)]
      (str uuid "." ext)
      (str uuid))))


(deftype FileSystemStorage [root-path]
  Storable

  (store! [this in-stream filename]
    ;; TODO: create dir if it doesn't exist
    (let [new-file (io/file root-path filename)]
      (when (.exists new-file)
        (throw (ex-info "Object already exists in storage"
                        {:type :duplicate-object})))
      (io/copy in-stream new-file)
      (path-relative-to-root root-path (.getPath new-file))))

  (retrieve! [this filename]
    (try
      (io/input-stream (io/file root-path filename))
      (catch FileNotFoundException e
        nil)))

  (dispose! [this filename]
    (try
      (io/delete-file filename)
      (catch IOException e
        false))))


(defstate ^:dynamic *storage*
  :start
  (let [root-dir (io/file (get env :media-path "media"))]
    (when-not (.exists root-dir)
      (.mkdir root-dir))
    (FileSystemStorage. (.getPath root-dir))))
