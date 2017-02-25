(ns channel.test.utils
  (:require [clojure.test :refer :all]
            [channel.utils :refer :all]))

(deftest any-nil?-test
  (testing "true for a collection of nil values"
    (is (any-nil? [nil nil nil])))
  (testing "true for a plain nil value"
    (is (any-nil? nil)))
  (testing "false for a collection of non-nil values"
    (is (not (any-nil? [1 2 3 4]))))
  (testing "false for a collection containing at least one non-nil value"
    (are [coll] (= (any-nil? coll) true)
      [1 2 nil 4]
      [1 nil nil 4]
      [nil nil nil 4])))

(deftest maybe-test
  (let [f (maybe (fn [& args] args))]
    (testing "calls function if no params are nil"
      (is (= (f 1 2 3) [1 2 3])))
    (testing "does't call function if any param is nil"
      (are [coll] (= (apply f coll) nil)
        [1 2 nil 4]
        [1 nil nil 4]
        [nil nil nil 4])))
  (let [called (atom false)
        f (maybe (fn [& _] (reset! called true)))]
    (testing "calls function if there are no parameters"
      (is (= (f) nil))
      (is (true? @called)))))
