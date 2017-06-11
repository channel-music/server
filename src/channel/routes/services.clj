(ns channel.routes.services
  (:require [channel.media :as media]
            [ring.util.http-response :as response]
            [compojure.api.sweet :refer :all]
            [schema.core :as s]))


(s/defschema Song
  {:title s/Str
   :album s/Str
   :artist s/Str})


(def songs (atom []))


(defapi service-routes
  {:swagger {:ui "/swagger-ui"
             :spec "/swagger.json"
             :data {:info {:version (System/getProperty "channel.version")
                           :title "Channel API"
                           :description "API for the Channel service"}}}}

  (context "/songs" []
    :tags ["songs"]

    (GET "/" []
      :return [Song]
      (response/ok @songs))

    (POST "/" []
      :return Song
      :body [song Song]
      :summary "Creates a new song"
      (swap! songs conj song)
      (response/created "/songs/1"))))
