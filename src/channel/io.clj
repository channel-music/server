(ns channel.io
  (:require [clojure.java.io :as io])
  (:import (java.nio.file Paths Path)))


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
