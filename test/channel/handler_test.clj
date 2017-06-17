(ns channel.handler-test
  (:require
   [channel.handler :refer [app]]
   [clojure.test :refer :all]
   [peridot.core :as mock]))


(deftest test-app
  (testing "not-found route"
    (let [response (-> (mock/session (app))
                       (mock/request "/invalid")
                       :response)]
      (is (= 404 (:status response)))
      (is (= {:detail "Not found"} (:body response))))))
