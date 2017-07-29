(ns clj-tm-api.core-test
  (:require [clojure.test :refer :all]
            [clj-tm-api.core :refer :all]))

(deftest url-test
  (testing "URI is generated correctly"
    (is (= 0 1))))
