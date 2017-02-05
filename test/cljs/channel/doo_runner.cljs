(ns channel.doo-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [channel.core-test]))

(doo-tests 'channel.core-test)

