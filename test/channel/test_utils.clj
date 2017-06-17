(ns channel.test-utils
  (:require
   [cheshire.core :as cheshire]
   [clojure.java.io :as io]
   [ring.util.codec :as codec])
  (:import
   (org.apache.http.entity ContentType)
   (org.apache.http.entity.mime MultipartEntity)
   (org.apache.http.entity.mime.content StringBody FileBody)
   (java.io ByteArrayOutputStream File)
   (java.nio.charset Charset)
   (javax.activation FileTypeMap)))


(defn test-resource
  "Fetch a file with the given `filename` from the test resources."
  [filename]
  (let [path (str "media/" filename)]
    (io/file (io/resource path))))


(defn parse-body
  "Parse a string (or input string) to a clojure object."
  [body]
  (cheshire/parse-string (slurp body) true))


;;
;; The following code is (mostly) taken from xeqi/peridot
;;
(defn- ensure-string [val]
  (let [val-str (if (keyword? val)
                  (name val)
                  (str val))]
    (codec/form-encode val-str)))


(defmulti add-entity-part
  (fn [multipart-entity key value] (type value)))


(defmethod add-entity-part File [entity key ^File file]
  (let [content-type (ContentType/create
                      (.getContentType
                       (FileTypeMap/getDefaultFileTypeMap) file))]
    (.addPart entity
              (ensure-string key)
              (FileBody. file content-type (.getName file)))))


(defmethod add-entity-part :default [entity key value]
  (.addPart entity
            (ensure-string key)
            (StringBody. (str value)
                         (Charset/forName "UTF-8"))))


(defn- make-mulipart-entity [params]
  (let [entity (MultipartEntity.)]
    (doseq [[key val] params]
      (add-entity-part entity key val))
    entity))


(defn map->form-str
  "Convert a clojure map to a form encoded string."
  [m]
  (let [entity (make-mulipart-entity m)
        out (ByteArrayOutputStream.)]
    (.writeTo entity out)
    (.close out)
    (.toString out)))

