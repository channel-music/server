(ns channel.storage
  (:require
   [channel.io :refer [str->path]]
   [clojure.java.io :as io]
   [clojure.string :as string])
  (:import (java.io FileNotFoundException IOException)))


(defprotocol Storable
  "Represents a way of persistently storing files."

  (store [this in-stream filename]
    "Copy a stream to storage, using `filename` as the unique identifier.
Returns the path relative to the storage location.")

  (retrieve [this filename]
    "Fetch a stream from storage using its relative file name.
Returns `nil` if the object does not exist.")

  (dispose [this filename]
    "Remove object from storage using its relative file name.
Returns `true` on success and `false` otherwise."))


(defn path-relative-to-root
  "Returns the final file path relative to the provided `root` path."
  [root relative]
  (if (string/starts-with? relative root)
    (str (.relativize (str->path root) (str->path relative)))
    (throw (ex-info
            "Can't give a path relative to a root that doesn't exist"
            {:root root, :relative relative}))))


(deftype FileSystemStorage [root-path]
  Storable

  (store [this in-stream filename]
    (let [new-file (io/file root-path filename)]
      (io/copy in-stream new-file)
      (path-relative-to-root root-path (.getPath new-file))))

  (retrieve [this filename]
    (try
      (io/input-stream (io/file root-path filename))
      (catch FileNotFoundException e
        nil)))

  (dispose [this filename]
    (try
      (io/delete-file filename)
      (catch IOException e
        false))))
