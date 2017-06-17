(ns channel.routes.services-test
  (:require
   [channel.handler :refer [app]]
   [channel.test-utils
    :refer [test-resource json-str->map map->json-str]]
   [channel.routes.services]
   [clojure.test :refer :all]
   [ring.mock.request :as mock]))


(deftest services-test
  (let [songs {"abcde" {:id "abcde",
                        :title "Transatlantic",
                        :album "Apricot Morning",
                        :artist "Quantic"}}]
    (with-redefs [channel.routes.services/songs (atom songs)]

      (testing "fetch all songs"
        (let [response ((app) (mock/request :get "/songs"))]
          (is (= 200 (:status response)))
          (is (= (vals songs) (json-str->map (:body response))))))

      (testing "fetch a song using ID"
        (let [response ((app) (mock/request :get "/songs/abcde"))]
          (is (= 200 (:status response)))
          (is (= (first (vals songs)) (json-str->map (:body response))))))

      (testing "returns not found for a missing ID"
        (let [response ((app) (mock/request :get "/songs/missing"))]
          (is (= 404 (:status response)))
          (is (= {:detail "Not found"} (json-str->map (:body response))))))

      (testing "replace a song using an ID"
        (let [new-song {:title "Transpacific", :album "Mandarin Morning", :artist "Quantic"}
              response ((app) (-> (mock/request :put "/songs/abcde")
                                  (mock/content-type "application/json")
                                  (mock/body (map->json-str new-song))))]
          (is (= 200 (:status response)))
          (let [body (json-str->map (:body response))]
            (is (= new-song (select-keys body (keys new-song)))))))

      (testing "returns not found when replacing a song with an invalid ID"
        (let [response ((app) (-> (mock/request :put "/songs/invalid")
                                  (mock/content-type "application/json")
                                  (mock/body (map->json-str {:title "Give me a 404!",
                                                             :album "Testing & Stuff"
                                                             :artist "Me and I"}))))]
          (is (= 404 (:status response)))
          (is (= {:detail "Not found"} (json-str->map (:body response))))))

      (testing "remove a song with an ID"
        (let [response ((app) (mock/request :delete "/songs/abcde"))]
          (is (= 204 (:status response))
              (= nil (:body response)))))

      (testing "returns not found when passed an invalid ID"
        (let [response ((app) (mock/request :delete "/songs/invalid"))]
          (is (= 404 (:status response)))
          (is (= {:detail "Not found"} (json-str->map (:body response))))))

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
          (is (= "invalid-audio-frame" (-> (:body response) json-str->map :type))))))))
