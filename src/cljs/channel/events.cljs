(ns channel.events
  (:require [channel.db :refer [app-state]]))

(def ^:private event-handlers (atom {}))

(defn reg-event
  "Register an event handler `f` with name `ev-name`. `f` will
  be receive the app state and the parameters passed by the caller.
  The returned map will become the new app state."
  [ev-name f]
  (when (get @event-handlers ev-name)
    (js/console.warn "Overwriting handler" ev-name))
  (swap! event-handlers assoc ev-name f))

(defn dispatch! [[ev-name & params]]
  (if-let [handler (get @event-handlers ev-name)]
    (swap! app-state handler params)
    ;; TODO: add proper CLJS logging
    (js/console.error "No handler for event" ev-name)))
