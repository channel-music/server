(ns sound-app.validation
  (:require [bouncer.core :as b]
            [bouncer.validators :as v]))

(defn format-validation-errors [errors]
  (->> errors
       first
       (map vec)
       (into {})
       not-empty))

(defn validate-create-song [song]
  (format-validation-errors
   (b/validate
    song
    :title [v/required]
    :file  [v/required]
    :track [v/required v/number])))
