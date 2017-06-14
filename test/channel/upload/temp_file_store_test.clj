(ns channel.upload.temp-file-store-test
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


;; TODO: test cleanup
