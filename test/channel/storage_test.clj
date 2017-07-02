(ns channel.storage-test
  (:require
   [channel.io]
   [channel.storage :as storage]
   [clojure.java.io :as io]
   [clojure.test :refer :all])
  (:import
   (clojure.lang ExceptionInfo)
   (channel.storage FileSystemStorage)
   (java.io StringReader FileNotFoundException)))


(deftest test-path-relative-to-root
  (testing "removes a single level of nesting"
    (is (= "child.txt" (storage/path-relative-to-root "root" "root/child.txt"))))

  (testing "removes multiple nests in nested relative path"
    (is (= "a/b/c.mp3" (storage/path-relative-to-root "root" "root/a/b/c.mp3"))))

  (testing "removes multiple nests when using nested root"
    (is (= "c.mp3" (storage/path-relative-to-root "root/a/b" "root/a/b/c.mp3"))))

  (testing "handles trailing slashes for root"
    (is (= "child.txt" (storage/path-relative-to-root "root/" "root/child.txt"))))

  (testing "throws when root is not the beginning of relative path"
    (is (thrown? ExceptionInfo (storage/path-relative-to-root "root" "a/b.wav")))))


(def test-fs-storage-dir
  (channel.io/path-join
   (channel.io/tmpdir)
   "channel-test"))


(use-fixtures :once
  (fn [f]
    (let [tmpdir (io/file test-fs-storage-dir)]
      (.mkdir tmpdir)
      (f)
      (channel.io/delete tmpdir))))


(deftest test-file-system-storage
  (let [fs-storage (FileSystemStorage. test-fs-storage-dir)]
    (testing "with mocked IO procedures"
      (testing "returns the relative file path when storing"
        (with-redefs [io/copy (constantly nil)]
          (is (= "test.txt" (storage/store fs-storage (StringReader. "test") "test.txt")))))

      (testing "returns the file contents when retrieving"
        (with-redefs [io/input-stream (constantly (StringReader. "test"))]
          (is (= "test" (slurp (storage/retrieve fs-storage "test.txt")))))))

    (testing "with real filesystem changes"
      (testing "creates a file when using store"
        (storage/store fs-storage (StringReader. "test") "test.txt")
        (is (.exists (io/file (.root-path fs-storage) "test.txt"))))

      (testing "retrieves an already existing file"
        (let [path (channel.io/path-join (.root-path fs-storage) "existing.txt")]
          (spit path "testing, 1, 2")
          (is (= "testing, 1, 2" (slurp (storage/retrieve fs-storage "existing.txt"))))
          (io/delete-file path)))

      (testing "throws when retrieving a file that doesn't exist"
        (is (nil? (storage/retrieve fs-storage "not-there.png"))))

      (testing "disposes an existing file"
        (let [path (channel.io/path-join (.root-path fs-storage) "delete-me.txt")]
          (spit path "to be deleted!")
          (is (storage/dispose fs-storage path))
          (is (not (.exists (io/file path))))))

      (testing "returns false when disposing a non-existant file"
        (is (not (storage/dispose fs-storage "i-dont-exist.png"))))))

  (let [fs-storage (FileSystemStorage. "invalid-dir")]
    (testing "fails if storage root doesn't exist"
      (is (thrown? FileNotFoundException
                   (storage/store fs-storage (StringReader. "test") "test.txt"))))))
