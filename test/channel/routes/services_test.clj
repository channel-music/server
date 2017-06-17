(ns channel.routes.services-test
  (:require
   [channel.handler :refer [app]]
   [channel.test-utils
    :refer [test-resource parse-body map->form-str]]
   [channel.routes.services]
   [clojure.test :refer :all]
   [ring.mock.request :as mock]))



(deftest services-test
  (let [songs [{:title "Transatlantic", :album "Apricon Morning", :artist "Quantic"}]]
    (with-redefs [channel.routes.services/songs (atom songs)]

      (testing "fetch all songs"
        (let [response ((app) (mock/request :get "/songs"))]
          (is (= 200 (:status response)))
          (is (= songs (parse-body (:body response))))))

      ;; TODO: Its currently too much effort to test using actual multipart files.
      ;; TODO: Move this to a integration-tests
      #_(testing "upload a valid music file"
        (let [media-file (test-resource "test.mp3")
              response ((app) (-> (mock/request :post "/upload")
                                  (mock/content-type "application/x-www-form-urlencoded")
                                  (mock/body (map->form-str {:file media-file}))))]
          (is (= 201 (:status response)))
          (is (= "http://localhost/songs/1" (get-in response [:headers "Location"])))
          (is (= nil (:body response)))))

      #_(testing "upload a corrupt music file"
        (let [media-file (test-resource "corrupt.mp3")
              response ((app) (-> (mock/request :post "/upload")
                                  (mock/content-type "application/x-www-form-urlencoded")
                                  (mock/body (map->form-str {:file media-file}))))]
          (is (= 400 (:status response)))
          (is (= "invalid-audio-frame" (-> (:body response) parse-body :type))))))))
