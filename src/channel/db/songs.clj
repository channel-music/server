(ns channel.db.songs
  (:require [hugsql.core :as hugsql]))


(hugsql/def-db-fns "sql/songs.sql")
