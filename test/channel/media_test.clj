(ns channel.media-test
  (:require
   [channel.test-utils :refer [test-resource]]
   [channel.media :as media]
   [clojure.test :refer :all]))


(def supported-formats ["mp3" "wav" "ogg" "flac"])


(deftest parse-media-file-test
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


(deftest parse-unsigned-int-test
  (testing "returns an integer when using a string containing an unsigned integer"
    (is (= 3 (media/parse-unsigned-int "3"))))

  (testing "returns nil when a signed integer is used"
    (is (= nil (media/parse-unsigned-int "-15"))))

  (testing "returns nil when using decimal values"
    (is (= nil (media/parse-unsigned-int "2.25"))))

  (testing "returns nil when using an invalid string"
    (is (= nil (media/parse-unsigned-int "a string"))))

  (testing "returns nil when using nil"
    (is (= nil (media/parse-unsigned-int nil)))))
