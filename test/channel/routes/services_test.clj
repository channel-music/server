(ns channel.routes.services-test
  (:require [clojure.test :refer :all]
            [cheshire.core :as cheshire]
            [ring.mock.request :as ring]
            [channel.handler :refer [app]]))


(defn parse-body [body]
  (cheshire/parse-string (slurp body) true))


(deftest services-test
  (let [songs [{:title "Transatlantic", :album "Apricon Morning", :artist "Quantic"}]]
    (with-redefs [channel.routes.services/songs (atom songs)]

      (testing "fetch all songs"
        (let [response ((app) (ring/request :get "/songs"))]
          (is (= 200 (:status response)))
          (is (= songs (parse-body (:body response))))))

      (testing "create a new song"
        (let [new-song {:title "Insomnia", :album "The Best Of", :artist "Faithless"}
              response ((app) (-> (ring/request :post "/songs")
                                  (ring/content-type "application/json")
                                  (ring/body (cheshire/generate-string new-song))))]
          (is (= 201 (:status response)))
          (is (= "http://localhost/songs/1" (get-in response [:headers "Location"])))
          (is (= nil (:body response))))))))
