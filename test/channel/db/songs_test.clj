(ns channel.db.songs-test
  (:require
   [channel.config]
   [channel.db.core :refer [*db*]]
   [channel.db.songs :as songs]
   [clojure.java.jdbc :as jdbc]
   [clojure.test :refer :all]
   [mount.core]))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Fixtures
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(use-fixtures
  :each
  ;; Start DB
  (fn [test-fn]
    (mount.core/start
     #'channel.config/env
     #'channel.db.core/*db*)
    (test-fn))
  ;; Setup transactions
  (fn [test-fn]
    (jdbc/with-db-transaction [conn *db*]
      (jdbc/db-set-rollback-only! conn)
      (binding [*db* conn] ;; rebind to use transactional conn
        (test-fn)))))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Tests
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(deftest test-songs
  (testing "create-song!"
    (is (number? (:id (songs/create-song! *db*
                                          {:title "We Didn't Start The Fire"
                                           :artist "Billy Joel"
                                           :album "Storm Front"
                                           :genre "Rock"
                                           :track 2
                                           :file "the-fire.mp3"}))))
    (is (number? (:id (songs/create-song! *db*
                                          {:title "Billy Jean"
                                           :artist "Michael Jackson"
                                           :album "Thriller"
                                           :genre "Pop"
                                           :track 6
                                           :file "billy-jean.mp3"})))))

  (testing "all-songs"
    (testing "fetching created objects"
      (is (<= 2 (count (songs/all-songs *db*)))))

    (testing "records are sorted by ID"
      (let [songs (songs/all-songs *db*)]
        (is (= songs (sort-by :id songs))))))

  (testing "song-by-id"
    (let [[{valid-id :id}] (songs/all-songs *db*)]
      (testing "valid ID"
        (is (= valid-id (:id (songs/song-by-id *db* {:id valid-id})))))

      (testing "missing ID"
        (is (= nil (songs/song-by-id *db* {:id (+ valid-id 3000)}))))))

  (testing "song-exists?"
    (testing "returns map if song is already stored"
      (is (map? (songs/song-exists? *db* {:title "Billy Jean"
                                          :artist "Michael Jackson"
                                          :album  "Thriller"}))))

    (testing "returns nil if no song exists"
      (is (nil? (songs/song-exists? *db* {:title "Johny B. Goode"
                                          :artist "Chuck Berry"
                                          :album "Around & Around"})))))

  (testing "update-song!"
    (testing "succeeds when all fields are present"
      (let [[song] (songs/all-songs *db*)
            new-song (assoc song :genre "Not sure")]
        (songs/update-song! *db* new-song)
        (is (not= song (songs/song-by-id *db* {:id (:id song)})))))

    (testing "fails if one or more fields are missing"
      (let [[song] (songs/all-songs *db*)]
        (is (thrown? clojure.lang.ExceptionInfo
                     (songs/update-song! *db* (dissoc song :genre))))
        (is (thrown? clojure.lang.ExceptionInfo
                     (songs/update-song! *db* (dissoc song :genre :title)))))))

  (testing "delete-song!"
    (let [[{song-id :id}] (songs/all-songs *db*)]
      (testing "succeeds if record exists"
        (songs/delete-song! *db* {:id song-id}))

      (testing "succeeds if record doesn't exist"
        (songs/delete-song! *db* {:id (+ song-id 3000)})))))
