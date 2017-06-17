(ns channel.handler-test
  (:require
   [channel.handler :refer [app]]
   [clojure.test :refer :all]
   [ring.mock.request :as mock]))


(deftest test-app
  (testing "not-found route"
    (let [response ((app) (mock/request :get "/invalid"))]
      (is (= 404 (:status response)))
      (is (= {:detail "Not found"} (:body response))))))
