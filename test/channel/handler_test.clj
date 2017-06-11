(ns channel.handler-test
  (:require [clojure.test :refer :all]
            [ring.mock.request :refer :all]
            [channel.handler :refer :all]))


(deftest test-app
  (testing "not-found route"
    (let [response ((app) (request :get "/invalid"))]
      (is (= 404 (:status response)))
      (is (= {:detail "Not found"} (:body response))))))
