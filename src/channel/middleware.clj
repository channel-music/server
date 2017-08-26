(ns channel.middleware
  (:require
   [channel.env :refer [defaults]]
   [ring.middleware.defaults :refer [api-defaults wrap-defaults]]))


(defn wrap-base [handler]
  (-> ((:middleware defaults) handler)
      (wrap-defaults
        (-> api-defaults
            (assoc-in [:security :anti-forgery] false)
            (dissoc :session)))))
