(ns channel.test.songs
  (:require [channel.songs :refer :all]
            [clojure.test :refer :all]
            [clojure.java.io :as io]))

;; TODO: Write a function to generate ID3 tags with the
;; TODO: given title, artist, etc. parameters.

#_(deftest file->song-test
  (testing "parses valid mp3 files"
    (let [song (file->song (io/file "./song.mp3"))]
      (testing "contains corret metadata"
        (testing "title"
          (is (= (:title song) "Spanish Caravan")))
        (testing "artist"
          (is (= (:artist song) "The Doors")))
        (testing "album"
          (is (= (:album song) "")))))))
