(ns channel.views.upload
  (:require [channel.db :refer [app-state]]
            [ajax.core :refer [POST]]))

;; FIXME
(defn upload-component []
  [:form#upload-form {:enc-type "multipart/form-data"
                      :method "POST"}
   [:label "Upload file:"]
   [:input#upload-file {:type "file"
                        :name "upload-file"}]])

(defn progress-bar [min max value]
  [:div.progress-bar {:role "progressbar"
                      :aria-valuenow value
                      :aria-valuemin min
                      :aria-valuemax max
                      :style {:width "60%"}}
   [:span.sr-only (str value "% Complete")]])

(defn upload-song! [target]
  (let [file (aget (.-files target) 0)
        form-data (doto (js/FormData.)
                    (.append "file" file))]
    (POST "/api/songs" {:body form-data
                        :handler #(swap! app-state update :songs conj %)
                        :error-handler #(println "Failed to upload file:" %)})))

(defn upload-page []
  [:div#upload
   [upload-component]
   [:button.btn.btn-default.btn-primary
    {:on-click #(upload-song!
                 (.getElementById js/document "upload-file"))}
    "Upload"]])


