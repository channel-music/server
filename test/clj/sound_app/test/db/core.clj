(ns sound-app.test.db.core
  (:require [sound-app.db.core :refer [*db*] :as db]
            [luminus-migrations.core :as migrations]
            [clojure.test :refer :all]
            [clojure.java.jdbc :as jdbc]
            [sound-app.config :refer [env]]
            [mount.core :as mount]))

(use-fixtures
  :once
  (fn [f]
    (mount/start
      #'sound-app.config/env
      #'sound-app.db.core/*db*)
    (migrations/migrate ["migrate"] (select-keys env [:database-url]))
    (f)))

(deftest test-users
  (jdbc/with-db-transaction [t-conn *db*]
    (jdbc/db-set-rollback-only! t-conn)
    (is (= 1 (db/create-user!
               t-conn
               {:id         1
                :email      nil
                :username   "sam-smith"
                :password   "pass"})))
    (is (= {:id         1
            :username   "sam-smith"
            :email      nil  ;; Email is optional
            :password   "pass"}
           (db/user-by-id t-conn {:id 1})))))
