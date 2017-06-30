(ns channel.io-test
  (:require
   [channel.io :as io]
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
