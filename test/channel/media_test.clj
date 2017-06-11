(ns channel.media-test
  (:require [channel.media :as media]
            [clojure.java.io :as io]
            [clojure.test :refer :all]))


(def supported-formats ["mp3" "wav" "ogg" "flac"])


(defn test-resource [filename]
  (let [url (io/resource (str "test-data/" filename))]
    (io/file (.getPath url))))


(deftest media-test
  (doseq [format supported-formats]
    (testing (str "parses " format " format")
      (let [metadata (->> (str "test." format)
                          test-resource
                          media/parse-media-file)]
        (is (= metadata {:title "Test Song"
                         :album "Channel Tests"
                         :artist "Antonis Kalou"
                         :genre  "Testing"
                         :year "2017"
                         :track-number 1
                         :total-tracks 1})))))

  (testing "throws exception when using invalid file format"
    (try
      (media/parse-media-file (test-resource "invalid.txt"))
      (is false "Parsing invalid file should have failed")
      (catch Exception e
        (is (= :cannot-read (:type (ex-data e)))))))

  (testing "throws exception if file does not contain tags"
    (try
      (media/parse-media-file (test-resource "no-tag.mp3"))
      (is false "Parsing media with no tag should have failed")
      (catch Exception e
        (is (= :missing-metadata (:type (ex-data e)))))))

  (testing "throws exception with corrupted media file"
    (try
      (media/parse-media-file (test-resource "corrupt.mp3"))
      (is false "Parsing corrupt media file should have failed")
      (catch Exception e
        (is (= :invalid-audio-frame (:type (ex-data e))))))))
