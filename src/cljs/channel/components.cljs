(ns channel.components
  "Reusable view components."
  (:require [rum.core :as rum]))

(def ^:private menu-toggle-mixin
  {:did-mount (fn [state]
                (set! (.-onclick (rum/dom-node state))
                      (fn [e]
                        (.preventDefault e)
                        (-> (.getElementById js/document "wrapper")
                            (.-classList)
                            (.toggle "toggled")))))})

;; TODO: private?
(rum/defc menu-toggle < menu-toggle-mixin [text]
  [:a#menu-toggle text])

(rum/defc sidebar < rum/static [links]
  [:#sidebar-wrapper
   [:ul.sidebar-nav
    [:li.sidebar-brand
     {:key (hash "root")} ;; TODO: find more robust method
     [:a {:href "#"} "Channel"]]
    (for [[name href] links]
      [:li {:key (hash name)}
       [:a {:href href} name]])
    #_[:li {:key "toggle"}
     (menu-toggle "<<")]]])
