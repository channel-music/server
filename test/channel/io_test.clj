(ns channel.io-test
  (:require
   [channel.io :as cio]
   [clojure.java.io]
   [clojure.test :refer :all]))


(deftest test-path-join
  (testing "with plain paths"
    (is (= "a/b/c.txt" (cio/path-join "a" "b" "c.txt"))))

  (testing "with trailing slashes"
    (is (= "a/b/c.txt" (cio/path-join "a/" "b/" "c.txt"))))

  (testing "with only one path"
    (is (= "test.txt" (cio/path-join "test.txt"))))

  (testing "when no paths are passed"
    (is (thrown? AssertionError (cio/path-join)))))


(deftest test-str->path
  (testing "with plain file"
    (let [path (cio/str->path "project.clj")]
      (is (instance? java.nio.file.Path path))
      (is (= "project.clj" (.toString (.getFileName path))))))

  (testing "with directory"
    (let [path (cio/str->path "src")]
      (is (instance? java.nio.file.Path path))
      (is (= "src" (.toString (.getFileName path))))))

  (testing "with trailing slashes"
    (let [path (cio/str->path "src/")]
      (is (instance? java.nio.file.Path path))
      (is (= "src" (.toString (.getFileName path)))))))


(deftest test-delete
  (testing "with a plain file"
    (spit "plain-file.txt" "abcde")
    (let [f (clojure.java.io/file "plain-file.txt")]
      (is (.exists f))
      (is (cio/delete f))
      (is (not (.exists f)))))

  (testing "with an empty directory"
    (let [dir (clojure.java.io/file "test-dir")]
      (.mkdir dir)
      (is (.exists dir))
      (is (cio/delete dir))
      (is (not (.exists dir)))))

  (testing "with a non-empty directory"
    (let [dir (clojure.java.io/file "test-dir")]
      (.mkdir dir)
      (spit (cio/path-join "test-dir" "file1.txt") "file1")
      (spit (cio/path-join "test-dir" "file2.txt") "file2")
      (is (cio/delete dir))
      (is (not (.exists dir)))))

  (testing "with a non-existent directory"
    (is (not (cio/delete (clojure.java.io/file "doesnt-exist"))))))


(deftest test-file-extension
  (testing "file with no extension"
    (is (= nil (cio/file-extension (clojure.java.io/file "plain-file")))))

  (testing "file with standard extension"
    (is (= "txt" (cio/file-extension (clojure.java.io/file "plain-file.txt")))))

  (testing "malformed file"
    (is (= nil (cio/file-extension (clojure.java.io/file "plain-file."))))))
