(ns channel.validation
  (:require [bouncer.core :as b]
            [bouncer.validators :as v]
            #?(:clj [channel.db.core :as db])))

(v/defvalidator file-format-validator
  {:default-message-format "%s is not of the correct format."}
  [file]
  #?(:clj  (= (:content-type file) "audio/mpeg")
     :cljs true))

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
    :file  [v/required file-format-validator])))

(defn validate-update-song [song]
  (format-validation-errors
   (b/validate
    song
    :title [v/required file-format-validator]
    :track [v/required])))
