(ns channel.routes.services.auth
  (:require [channel.db.core :as db]
            [clojure.tools.logging :as log]
            [schema.core :as s]
            [ring.util.http-response :as ring-response]))

(s/defschema User {:id       s/Int
                   :username s/Str
                   :email    (s/maybe s/Str)})

(defn login [username password {:keys [remote-addr]}]
  (if-let [user (-> (db/user-by-username {:username username})
                    (dissoc :password))]
    (ring-response/ok user)
    (do
      ;; Log failed logins to monitor against attacks.
      ;; TODO: make this toggleable, some users may not
      ;; TODO: want ANY addresses tracked
      (log/info "login failed for" username remote-addr)
      (ring-response/not-found))))
