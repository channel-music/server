(ns channel.views.upload
  (:require [ajax.core :refer [POST]]
            [channel.views.components :as c]
            [rum.core :as rum]))

(extend-type js/FileList
  ISeqable
  (-seq [files] (array-seq files 0)))

(defn- bytes->megabytes
  "Converts from bytes representation to megabytes."
  [bytes]
  (if (zero? bytes)
    0
    (/ bytes 1024.0 1024.0)))

(defn- wrap-native-event
  "Wrapper for react event handler. Calls `f` with
  the native DOM node rather than the react SyntheticEvent."
  [f]
  (fn [e]
    (f (.-nativeEvent e))))

(defn- wrap-prevent-default
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

(rum/defc file-form [db files]
  [:form.form-inline {:enc-type "multipart/form-data"
                      :method "POST"
                      :on-submit (wrap-prevent-default
                                  (fn [_]
                                    (doseq [file @files]
                                      (upload-song! (rum/cursor db :songs) file))))}
   [:div.form-group
    [:input {:type "file", :multiple true
             :on-change (wrap-native-event
                         (fn [e]
                           (swap! files into (set (-> e .-target .-files)))))}]]
   [:button.btn.btn-default {:type :submit}
    [:i.fa.fa-upload]]])

(rum/defc file-list [files]
  [:table.table
   [:thead
    [:tr
     [:th "Name"]
     [:th "Size"]
     [:th "Progress"]
     [:th]]]
   [:tbody
    (for [file @files]
      [:tr {:key (hash file)}
       [:td (.-name file)]
       [:td (-> (.-size file)
                (bytes->megabytes)
                (.toPrecision 3)
                (str "MB"))]
       [:td (c/progress 60)]
       [:td [:button.btn.btn-default
             {:on-click #(swap! files disj file)}
             [:i.fa.fa-trash]]]])]])

(rum/defcs upload-page < (rum/local #{} ::files)
  [state db]
  [:#upload
   [:.row
    [:.col-md-12
     (file-list (::files state))]]
   [:row
    [:col-md-12 (c/progress 50)]]
   [:.row
    [:.col-md-12
     (file-form db (::files state))]]])
