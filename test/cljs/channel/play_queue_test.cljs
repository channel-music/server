(ns .channel.play-queue-test
  (:require [channel.play-queue :refer :all]
            [cljs.test :refer :all :include-macros true]))

(deftest play-queue-test
  (let [songs [{:id 1, :title "My Iron Lung", :artist "Radiohead"}
               {:id 2, :title "Talk Show Host", :artist "Radiohead"}]
        play-queue (make-play-queue songs)]
    (testing "uses song ID's"
      (is (= (sort (z/root q)) [1 2])))

    (testing "the current ID is set to the first track"
      (is (= (track-id play-queue) 1)))

    (testing "moving to the next track"
      (let [shifted-queue (next-track play-queue)]
        (testing "returns a new play queue"
          (is (not= shifted-queue play-queue)))
        (testing "has a new track ID"
          (is (= (track-id shifted-queue) 2)))))))
