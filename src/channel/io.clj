(ns channel.io
  (:require [clojure.java.io :as io])
  (:import
   (java.nio.file Paths Path)
   (org.apache.commons.io FileUtils FilenameUtils)))


(defn tmpdir
  "Returns the temporary directory used by the operating system."
  []
  (System/getProperty "java.io.tmpdir"))


(defn path-join
  "Returns a string of all file paths joined together."
  [& paths]
  {:pre [(seq paths)]}
  (.toString (apply io/file paths)))


(defn str->path
  "Convert a plain string to a java `Path` object."
  ^Path [^String s]
  (Paths/get (.toURI (io/file s))))


(defn delete
  "Delete a file or directory. If the directory is not empty it
  will be deleted regardless. Returns `true` on success and `false`
  on failure."
  [^java.io.File file]
  (if (.isDirectory file)
    (try
      (FileUtils/deleteDirectory file)
      true
      (catch IllegalArgumentException e
        false))
    (.delete file)))


(defn file-extension
  "Returns the extension of the given file or `nil` if it doesn't have one."
  [^java.io.File file]
  (let [ext (FilenameUtils/getExtension (.getPath file))]
    (if (empty? ext) nil ext)))
