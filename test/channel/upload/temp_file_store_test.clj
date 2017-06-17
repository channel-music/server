(ns channel.upload.temp-file-store-test
  "Tests for channel.upload.temp-file-store. These tests are nearly
  identical to those provided for a similar store in ring-core."
  (:require
   [channel.upload.temp-file-store :refer [temp-file-store]]
   [clojure.test :refer :all]
   [ring.util.io :refer [string-input-stream]])
  (:import [org.apache.commons.io FilenameUtils]))


(deftest test-temp-file-store
  (let [store (temp-file-store)
        result (store
                {:filename "foo.txt"
                 :content-type "text/plain"
                 :stream (string-input-stream "foo")})]
    (is (= "foo.txt" (:filename result)))
    (is (= "text/plain" (:content-type result)))
    (is (= 3 (:size result)))
    (is (instance? java.io.File (:tempfile result)))
    (is (= "txt" (-> (:tempfile result)
                     (.toString)
                     (FilenameUtils/getExtension))))
    (is (.exists (:tempfile result)))
    (is (= (slurp (:tempfile result)) "foo"))))


(defn eventually [check n delay]
  (loop [i n]
    (if (check)
      true
      (when (pos? i)
        (Thread/sleep delay)
        (recur (dec i))))))


(deftest test-temp-file-expiry
  (let [store (temp-file-store {:expires-in 2})
        result (store
                {:filename "foo.txt"
                 :content-type "text/plain"
                 :stream (string-input-stream "foo")})]
    (is (.exists (:tempfile result)))
    (Thread/sleep 2000)
    (let [deleted? (eventually #(not (.exists (:tempfile result))) 120 250)]
      (is deleted?))))


(defn thread-stacktrace []
  (.keySet (Thread/getAllStackTraces)))


(deftest test-temp-file-threads
  (let [threads0 (thread-stacktrace)
        store (temp-file-store)
        threads1 (thread-stacktrace)]
    (is (= (count threads0)
           (count threads1)))
    (dotimes [_ 200]
      (store {:filename "foo.txt"
              :content-type "text/plain"
              :stream (string-input-stream "foo")}))
    (is (< (count (thread-stacktrace)) 100))))
