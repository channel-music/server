(ns channel.validation
  (:require [bouncer.core :as b]
            [bouncer.validators :as v]
            #?(:clj [channel.db.core :as db])))


#?(
:clj
(defn- valid-file-extension?
  [file] true)
:cljs
(defn- valid-file-extension?
  [file] true)
)

#?(:clj
;; TODO: Set up actual bouncer validator
(defn validate-unique-song
  "Validates that `song` is unique. Will return `nil` if unique, otherwise
will return a map containing errors."
  [song]
  (when (:exists (db/song-exists? song))
    {:unique ["Song with that title, artist and album already exists."]}))
)

(v/defvalidator file-format-validator
  {:default-message-format "%s is not of the correct format."}
  [file]
  (valid-file-extension? file))

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
    :file  [v/required file-format-validator]
    :track [v/required v/number])))

(defn validate-update-song [song]
  (format-validation-errors
   (b/validate
    song
    :title [v/required file-format-validator]
    :track [v/required])))
