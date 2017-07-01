(ns channel.io-test
  (:require
   [channel.io :as io]
   [clojure.java.io]
   [clojure.test :refer :all]))


(deftest test-path-join
  (testing "with plain paths"
    (is (= "a/b/c.txt" (io/path-join "a" "b" "c.txt"))))

  (testing "with trailing slashes"
    (is (= "a/b/c.txt" (io/path-join "a/" "b/" "c.txt"))))

  (testing "with only one path"
    (is (= "test.txt" (io/path-join "test.txt"))))

  (testing "when no paths are passed"
    (is (thrown? AssertionError (io/path-join)))))


(deftest test-str->path
  (testing "with plain file"
    (let [path (io/str->path "project.clj")]
      (is (instance? java.nio.file.Path path))
      (is (= "project.clj" (.toString (.getFileName path))))))

  (testing "with directory"
    (let [path (io/str->path "src")]
      (is (instance? java.nio.file.Path path))
      (is (= "src" (.toString (.getFileName path))))))

  (testing "with trailing slashes"
    (let [path (io/str->path "src/")]
      (is (instance? java.nio.file.Path path))
      (is (= "src" (.toString (.getFileName path)))))))


(deftest test-delete
  (testing "with a plain file"
    (spit "plain-file.txt" "abcde")
    (let [f (clojure.java.io/file "plain-file.txt")]
      (is (.exists f))
      (is (io/delete f))
      (is (not (.exists f)))))

  (testing "with an empty directory"
    (let [dir (clojure.java.io/file "test-dir")]
      (.mkdir dir)
      (is (.exists dir))
      (is (io/delete dir))
      (is (not (.exists dir)))))

  (testing "with a non-empty directory"
    (let [dir (clojure.java.io/file "test-dir")]
      (.mkdir dir)
      (spit (io/path-join "test-dir" "file1.txt") "file1")
      (spit (io/path-join "test-dir" "file2.txt") "file2")
      (is (io/delete dir))
      (is (not (.exists dir)))))

  (testing "with a non-existent directory"
    (is (not (io/delete (clojure.java.io/file "doesnt-exist"))))))
