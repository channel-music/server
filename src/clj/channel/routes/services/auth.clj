(ns channel.routes.services.auth
  (:require [buddy.hashers :as hashers]
            [buddy.sign.jwt :as jwt]
            [channel.config :refer [env]]
            [channel.db.core :as db]
            [clojure.tools.logging :as log]
            [ring.util.http-response :as ring-response]
            [schema.core :as s]))

(s/defschema User {:id       s/Int
                   :username s/Str
                   :email    (s/maybe s/Str)})

(def LoginResponse (assoc User :token s/Str))

(defn create-auth-token [user]
  (jwt/sign user (env :jwt-secret)))

(defn login
  "Authenticate user using their `username` and `password`. Also
  expects the request map."
  [username password {:keys [remote-addr server-name]}]
  (if-let [user (db/user-by-username {:username username})]
    (if (hashers/check password (:password user))
      (ring-response/ok (-> user
                            (dissoc :password)
                            (assoc :token (create-auth-token user))))
      (do
        ;; Log failed logins to monitor against attacks.
        ;; TODO: make this toggleable, some users may not
        ;; TODO: want ANY addresses tracked
        (log/info "login failed for" username remote-addr server-name)
        (ring-response/unauthorized "Invalid login credentials")))
    (ring-response/not-found)))

(defn all-users
  "Fetch all users."
  []
  (->> (db/all-users)
       (map #(dissoc % :password))
       (ring-response/ok)))
