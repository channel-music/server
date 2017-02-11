(ns channel.views.upload
  (:require [ajax.core :refer [POST]]
            [rum.core :as rum]))

(extend-type js/FileList
  ISeqable
  (-seq [files] (array-seq files 0)))

(defn- wrap-native-event
  "Wrapper for react event handler. Calls `f` with
  the native DOM node rather than the react SyntheticEvent."
  [f]
  (fn [e]
    (f (.-nativeEvent e))))

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

(rum/defcs upload-page < (rum/local [] ::files)
  [state db]
  [:form {:enc-type "multipart/form-data"
          :method "POST"
          :on-submit (wrap-prevent-default
                      (fn [e]
                        (doseq [file @(::files state)]
                          (upload-song! (rum/cursor db :songs) file))))}
   [:em (pr-str state)]
   [:div.form-group
    [:label {:for "file"} "Upload file:"]
    [:input {:type "file", :id "file", :multiple true
             :on-change (wrap-native-event
                         (fn [e]
                           (let [files (vec (-> e .-target .-files))]
                             (reset! (::files state) files))))}]]
   [:button.btn.btn-default {:type :submit}
    "Upload"]])
