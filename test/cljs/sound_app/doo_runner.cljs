(ns sound-app.doo-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [sound-app.core-test]))

(doo-tests 'sound-app.core-test)

