(ns channel.upload.middleware
  (:require
   [channel.upload.temp-file-store :refer [temp-file-store]]
   [compojure.api.upload :as api.upload]))


(defn wrap-multipart-params
  "Same as `compojure.api.upload/wrap-multipart-params`, but uses
  a custom temporary file store that preserves the extension of the
  uploaded file."
  ([handler]
   (wrap-multipart-params handler {}))
  ([handler options]
   (api.upload/wrap-multipart-params
    handler
    (merge options {:store (temp-file-store)}))))
