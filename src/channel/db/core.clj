(ns channel.db.core
  (:require
   [channel.config :refer [env]]
   [hikari-cp.core :as hikari]
   [mount.core :refer [defstate]]
   [to-jdbc-uri.core :refer [to-jdbc-uri]]))


(defstate ^:dynamic *db*
  :start {:datasource (hikari/make-datasource
                       {:jdbc-url (to-jdbc-uri (env :database-url))
                        :pool-name "channel-pool"})}
  :stop  (when-let [ds (:datasource *db*)]
           (hikari/close-datasource ds)))


