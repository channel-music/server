(ns channel.events
  (:require [channel.db :refer [app-state]]))

(defmulti handle-event
  "Register an event handler. Will receive the app state
  and the parameters passed by the caller. The returned map
  will become the new app state."
  {:default nil}
  (fn [k & _] k))

;; Default handler
(defmethod handle-event nil
  [ev-name & _]
  ;; TODO: add proper CLJS logging
  (js/console.error "No handler for event:" ev-name))

(defn dispatch! [[ev-name & params]]
  (swap! app-state #(apply handle-event ev-name % params)))
