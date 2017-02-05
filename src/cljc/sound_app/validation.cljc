(ns sound-app.validation
  (:require [bouncer.core :as b]
            [bouncer.validators :as v]
            #?(:clj [clojure.java.io :as io])))


#?(
:clj
(defn- valid-file-extension?
  [file] true)

:cljs
(defn- valid-file-extension?
  [file] true)
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
