(ns channel.views.upload
  (:require [ajax.core :refer [POST]]
            [rum.core :as rum]))

(defn- synthetic-event-dom-node
  "Returns the related DOM node for a react SyntheticEvent."
  [e]
  (.-target (.-nativeEvent e)))

(defn- wrap-dom-node
  "Wrapper for react event handler. Gets the events' related
  DOM node."
  [f]
  (fn [e]
    (f e (synthetic-event-dom-node e))))

(defn wrap-prevent-default
  "Wrapper for react event handler. Always prevent default
  on the received event, even if an exception is thrown when
  calling `f`."
  [f]
  (fn [e]
    (try (f e)
      (finally
        (.preventDefault e)))))

(defn upload-song! [songs file]
  (let [form-data (doto (js/FormData.)
                    (.append "file" file))]
    (POST "/api/songs" {:body form-data
                        :handler #(swap! songs conj %)
                        :error-handler #(println "Failed to upload file:" %)})))

(rum/defcs upload-page
  [state db]
  [:form {:enc-type "multipart/form-data"
          :method "POST"
          :on-submit ((comp wrap-prevent-default wrap-dom-node)
                      (fn [e dom-node]
                        (let [file-input (.-file dom-node)]
                          (let [file (aget (.-files file-input) 0)]
                            (js/console.log "Uploading file:" file)
                            (upload-song! (rum/cursor db :songs) file)))))}
   [:div.form-group
    [:label {:for "file"} "Upload file:"]
    [:input {:type "file", :id "file", :name "file"}]]
   [:button.btn.btn-default {:type :submit}
    "Upload"]])
