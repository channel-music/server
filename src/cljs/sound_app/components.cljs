(ns sound-app.components
  "Reusable view components."
  (:require [reagent.core :as reagent]))

(defn sidebar [links]
  [:div#sidebar-wrapper
   [:ul.sidebar-nav
    ^{:key (hash "root")} ;; TODO: find more robust method
    [:li.sidebar-brand
     [:a {:href "#"} "Sound App"]]
    (for [[name href] links]
      ^{:key (hash name)}
      [:li [:a {:href href} name]])]]) 

(defn- menu-toggle-render [child]
  [:div.btn.btn-default child])

(defn- menu-toggle-did-mount [this]
  (set! (.-onclick (reagent/dom-node this))
        (fn [e]
          (.preventDefault e)
          (-> (.getElementById js/document "wrapper")
              (.-classList)
              (.toggle "toggled")))))

(defn menu-toggle []
  (reagent/create-class {:reagent-render menu-toggle-render
                         :component-did-mount menu-toggle-did-mount}))
