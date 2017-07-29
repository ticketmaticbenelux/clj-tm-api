(ns clj-tm-api.core-test
  (:require [clojure.test :refer :all]
            [clj-tm-api.core :refer :all]))

(def client {:account "ticketmatic"
             :key "xyz"
             :secret "xyz"})

(deftest signature-test
  (testing "Signature is generated correctly"
    (with-redefs [current-datetime (fn [] "2017-07-29T16:40:46")]
      (is (= "TM-HMAC-SHA256 key=xyz ts=2017-07-29T16:40:46 sign=192437c5f28b8dd431c57bac1f3552d671cd4eb50382961dfb413d731d050590"
             (gen-signature client))))))

(deftest endpoint-test
  (testing "Endpoint is generated correctly"
    (is (= "contacts" (gen-uri-endpoint :contacts)))))

(deftest uri-test
  (testing "URI is generated correctly"
    (is (= "https://apps.ticketmatic.com/api/1/ticketmatic/contacts/10002" (gen-uri client :contacts 10002)))))
