(ns channel.routes.services-test
  (:require
   [channel.config]
   [channel.db.core :refer [*db*]]
   [channel.db.songs :as songs]
   [channel.handler :refer [app]]
   [channel.test-utils
    :refer [test-resource json-str->map map->json-str]]
   [channel.routes.services :as services]
   [channel.test-utils :refer [test-resource make-mount-fixture]]
   [clojure.java.io :as io]
   [clojure.java.jdbc :as jdbc]
   [clojure.test :refer :all]
   [ring.mock.request :as mock]
   [mount.core]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Fixtures
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;



;; FIXME: duplicated with channel.db.songs-test
(use-fixtures
  :each
  ;; Start DB
  (make-mount-fixture
   #'channel.config/env
   #'channel.db.core/*db*)
  ;; Setup transactions
  (fn [test-fn]
    (jdbc/with-db-transaction [conn *db*]
      (jdbc/db-set-rollback-only! conn)
      (binding [*db* conn] ;; rebind to use transactional conn
        (test-fn)))))

(use-fixtures
  :once
  (make-mount-fixture #'channel.storage/*storage*))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Helpers
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn- without-file
  "Return a list of songs with their file's removed."
  [songs]
  (map #(dissoc % :file) songs))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Tests
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(deftest test-make-song
  ;; Start storage service
  (mount.core/start #'channel.storage/*storage*)

  (let [metadata {:title "Transatlantic",
                  :album "Apricot Morning",
                  :artist "Quantic"
                  :genre "Triphop"
                  :track 1}]
    (testing "creates a new song if none exists"
      (let [create-called? (atom false)]
        (with-redefs [channel.db.songs/song-exists?
                      (constantly nil)
                      channel.db.songs/create-song!
                      (fn [_ m]
                        (reset! create-called? true)
                        m)]
          (let [song (services/make-song metadata (test-resource "test.ogg"))]
            (is @create-called?)
            (is (map? song))
            (is (= metadata (select-keys song (keys metadata))))
            (is (contains? song :file))))))

    (testing "throws an exception if the song already exists"
      (let [create-called? (atom false)]
        (with-redefs [channel.db.songs/song-exists?
                      (fn [_ m] m)
                      channel.db.songs/create-song!
                      (fn [_ m]
                        (reset! create-called? true)
                        m)]
          (try
            (services/make-song metadata (test-resource "test.ogg"))
            (catch clojure.lang.ExceptionInfo e
              (is (= :duplicate-record (:type (ex-data e)))))))))))


(deftest test-song-with-url
  (with-redefs [channel.config/env {:media-url "test-media"}]
    (testing "appends current media url to file"
      (is (= {:file "test-media/test.txt"}
             (services/song-with-url {:file "test.txt"}))))

    (testing "ignores songs with no file"
      (is (= {:title "a"} (services/song-with-url {:title "a"}))))

    (testing "ignores songs with empty file string"
      (is (= {:file ""} (services/song-with-url {:file ""}))))))


(deftest services-test
  (let [songs (map #(merge (songs/create-song! *db* %) %)
                   [{:title "Transatlantic",
                     :album "Apricot Morning",
                     :artist "Quantic"
                     :genre "Triphop"
                     :track 1
                     :file "test-file.mp3"}])
        song-id (:id (first songs))
        song-url (format "/songs/%d" song-id)
        missing-song-url (format "/songs/%d" (+ song-id 3000))]
    (testing "fetch all songs"
      (let [response ((app) (mock/request :get "/songs"))]
        (is (= 200 (:status response)))
        ;; DB may be poluted, so check subset instead
        (is (clojure.set/subset?
             (set (without-file songs))
             (set (without-file (json-str->map (:body response))))))))

    (testing "fetch a song using ID"
      (with-redefs [channel.config/env {:media-url "test-media"}]
        (let [response ((app) (mock/request :get song-url))]
          (testing "correct status"
            (is (= 200 (:status response))))

          (let [resp (json-str->map (:body response))]
            (testing "contains expected song data"
              (is (= (without-file songs) (without-file [resp]))))

            (testing "includes media path in file"
              (is (clojure.string/includes? (:file resp) "test-media")))))))

    (testing "returns not found for a missing ID"
      (let [response ((app) (mock/request :get missing-song-url))]
        (is (= 404 (:status response)))
        (is (= {:detail "Not found"} (json-str->map (:body response))))))

    (testing "returns bad request for a badly formed ID"
      (let [response ((app) (mock/request :get "/songs/invalid"))]
        (is (= 400 (:status response)))
        (is (contains? (json-str->map (:body response)) :errors))))

    (testing "replace a song using an ID"
      (let [new-song {:title "Transpacific", :album "Mandarin Morning", :artist "Quantic",
                      :genre "Triphop", :track 1}
            response ((app) (-> (mock/request :put song-url)
                                (mock/content-type "application/json")
                                (mock/body (map->json-str new-song))))]
        (is (= 200 (:status response)))
        (let [body (json-str->map (:body response))]
          (is (= new-song (select-keys body (keys new-song)))))))

    (testing "returns not found when replacing a song with an invalid ID"
      (let [response ((app) (-> (mock/request :put missing-song-url)
                                (mock/content-type "application/json")
                                (mock/body (map->json-str {:title "Give me a 404!",
                                                           :album "Testing & Stuff"
                                                           :artist "Me and I"
                                                           :genre "Jazz"
                                                           :track 3}))))]
        (is (= 404 (:status response)))
        (is (= {:detail "Not found"} (json-str->map (:body response))))))

    (testing "remove a song with an ID"
      (let [response ((app) (mock/request :delete song-url))]
        (is (= 204 (:status response))
            (= nil (:body response)))))

    (testing "returns not found when passed an invalid ID"
      (let [response ((app) (mock/request :delete missing-song-url))]
        (is (= 404 (:status response)))
        (is (= {:detail "Not found"} (json-str->map (:body response))))))))

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
          (is (= "invalid-audio-frame" (-> (:body response) json-str->map :type)))))

