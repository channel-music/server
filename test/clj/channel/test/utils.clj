(ns channel.test.utils
  (:require [clojure.test :refer :all]
            [channel.utils :refer :all]))

(deftest any-nil?-test
  (testing "true for a collection of nil values"
    (is (any-nil? [nil nil nil])))
  (testing "false for a plain nil value"
    (is (not (any-nil? nil))))
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
    (testing "doesn't call function if any param is nil"
      (are [coll] (= (apply f coll) nil)
        [1 2 nil 4]
        [1 nil nil 4]
        [nil nil nil 4])))
  (let [f (maybe (fn [& _] :called))]
    (testing "calls function if there are no parameters"
      (is (= (f) :called)))))

(deftest sorted-map-by-value
  (testing "with a nested map"
    (testing "single key"
      (let [m {1 {:priority 5}, 2 {:priority 3}, 3 {:priority 10}}]
        (is (= (map->sorted-map :priority m)
               {2 {:priority 3}, 1 {:priority 5}, 3 {:priority 10}}))))
    (testing "multiple keys with priority"
      (let [m {1 {:track 1, :artist "Black Sabbath", :album "Black Sabbath"}
               2 {:track 3, :artist "Black Sabbath", :album "Paranoid"}
               3 {:track 1, :artist "Black Sabbath", :album "Paranoid"}
               4 {:track 2, :artist "Black Sabbath", :album "Black Sabbath"}}]
        (is (= (map->sorted-map (juxt :artist :album :track) m)
               {1 {:track 1, :artist "Black Sabbath", :album "Black Sabbath"}
                4 {:track 2, :artist "Black Sabbath", :album "Black Sabbath"}
                3 {:track 1, :artist "Black Sabbath", :album "Paranoid"}
                2 {:track 3, :artist "Black Sabbath", :album "Paranoid"}}))))))
