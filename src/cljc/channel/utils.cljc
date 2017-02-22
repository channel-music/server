(ns channel.utils
  "General utilities useful for both the frontend and the backend.")

(defn any-nil?
  "Returns true if any items in collection `coll` are nil."
  [coll]
  (some (comp not nil?) coll))

(defn maybe
  "Returns a function that calls `f` with all parameters, given
  that one or more parameters are not nil."
  [f]
  (fn [& args]
    (when (any-nil? args)
      (apply f args))))
