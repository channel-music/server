(ns channel.upload.temp-file-store
  "A multipart storage engine for storing uploads in temporary files.
  Does the same as the ring middleware store, but preserves file extension."
  (:require
   [clojure.java.io :as io])
  (:import
   (java.io File)
   ;; TODO: add explicit dependency
   (org.apache.commons.io FilenameUtils)))


(defn- background-thread [^Runnable f]
  (doto (Thread. f)
    (.setDaemon true)
    (.start)))


(defmacro ^{:private true} do-every [delay & body]
  `(background-thread
    #(while true
       (Thread/sleep (* ~delay 1000))
       (try ~@body
            (catch Exception e#)))))


(defn- expired? [^File file expires-in]
  (< (.lastModified file)
     (- (System/currentTimeMillis)
        (* expires-in 1000))))


(defn- remove-old-files [files expires-in]
  (doseq [^File file @files]
    (when (expired? file expires-in)
      (.delete file)
      (swap! files disj file))))


(defn- start-clean-up [files expires-in]
  (when expires-in
    (do-every expires-in
      (remove-old-files files expires-in))))


(defn- ensure-shutdown-cleanup [files]
  (.addShutdownHook
   (Runtime/getRuntime)
   (Thread.
    #(doseq [^File file @files]
       (.delete file)))))


(defn- ^File make-temp-file [files filename]
  (let [extension (let [ext (FilenameUtils/getExtension filename)]
                    (if (empty? ext)
                      nil
                      (str "." ext)))
        temp-file (File/createTempFile "channel-multipart-" extension)]
    (swap! files conj temp-file)
    temp-file))


(defn temp-file-store
  ([] (temp-file-store {:expires-in 3600}))
  ([{:keys [expires-in]}]
   (let [files (atom #{})
         clean-up (delay (start-clean-up files expires-in))]
     (ensure-shutdown-cleanup files)
     (fn [item]
       (force clean-up)
       (let [temp-file (make-temp-file files (:filename item))]
         (io/copy (:stream item) temp-file)
         (-> (select-keys item [:filename :content-type])
             (assoc :tempfile temp-file
                    :size (.length temp-file))))))))
