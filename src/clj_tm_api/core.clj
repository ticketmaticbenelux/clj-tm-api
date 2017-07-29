(ns clj-tm-api.core
  (:require [java-time :as time])
  (:require [pandect.algo.sha256 :refer :all])
  (:require [clojure.data.json :as json])
  (:require [clojure.core.async :as async])
  (:require [clj-http.client :as client]))

;; More info:
;; https://stackoverflow.com/questions/18662301/make-clojures-println-thread-safe-in-the-same-way-as-in-java
(defn safe-println [str & more] (locking *out* (apply println str more)))

; Get current ts in the TM3 API expected format
(defn current-datetime []
  (time/format "YYYY-MM-dd'T'HH:mm:ss"
               (time/zoned-date-time
                 (time/zone-id "UTC"))))

; Generate signature
(defn gen-signature [client]
  (format "TM-HMAC-SHA256 key=%s ts=%s sign=%s"
    (client :key)
    (current-datetime)
    (sha256-hmac
      (str (client :key) (client :account) (current-datetime))
      (client :secret))))

; Generate URI
(defn gen-uri-endpoint
  ([endpoint]
      (case endpoint
        :contacts "contacts"
        :contactsimport "contacts/import"
        :contactsreserve "contacts/import/reserve"
        :relationtypes "contacts/import/reserve"
        :events "events"
        :locations "settings/events/eventlocations"
        :orders "orders"
        :import "orders/import"
        :reserve "orders/import/reserve"
        :products "settings/products"
        :pricetypes "settings/pricing/pricetypes"
        :pricelists "settings/pricing/pricelists"
        :orderfeedefinitions "settings/pricing/orderfeedefinitions"
        :customfields "settings/system/customfields"
        :customfieldvalues "settings/system/customfieldvalues"
        :fielddefinitions "settings/system/fielddefinitions"
        :filterdefinitions "settings/system/filterdefinitions"
        :views "settings/system/views"
        :reports "settings/system/reports"
        :webskins "settings/communicationanddesign/webskins"
        :ordermails "settings/communicationanddesign/ordermails"
        :ticketlayouts "settings/communicationanddesign/ticketlayouts"
        :ticketlayouttemplates "settings/communicationanddesign/ticketlayouttemplates"
        :documents "settings/communicationanddesign/documents"
        :locktypes "settings/ticketsales/locktypes"
        :vouchers "settings/vouchers"
        :queries "tools/queries"
        :export "tools/queries/export"
        ))

  ([endpoint id]
   (let [template
         (case endpoint
           :eventlocktickets "events/%d/tickets/lock"
           :eventunlocktickets "events/%d/tickets/unlock"
           :eventseatranks "events/%d/tickets/seatrank"
           :ticketsbatch "events/%d/tickets/batch"
           :logs "orders/%d/logs"
           :pdf "orders/%d/pdf"
           :tickets "orders/%d/tickets"
           :payments "orders/%d/payments"
           :confirm "orders/%d"
           :emailpaymentinstruction "orders/%d/tickets/emailpaymentinstruction"
           :emaildelivery "orders/%d/tickets/emaildelivery"
           :vouchercodes "settings/vouchers/%d/codes"
           (str (gen-uri-endpoint endpoint) "/%d"))]
   (format template id)))

  ([endpoint id id-2 lang]
   (case endpoint
     :orderdocuments (format "orders/%d/documents/%d/%s" id id-2 lang))))

;(gen-uri-endpoint :contacts)
;(gen-uri-endpoint :contacts 10000)
;(gen-uri-endpoint :contacts 10000 1)
;(gen-uri-endpoint :eventlocktickets 10009)
;(gen-uri-endpoint :orderdocuments 1 10000 "nl")

(defn gen-uri [client endpoint & args]
  (str (format "https://apps.ticketmatic.com/api/1/%s/" (client :account))
       (apply gen-uri-endpoint endpoint args)))

;(gen-uri client :contacts)
;(gen-uri client :contacts 10002)
;(gen-uri client :eventlocktickets 10009)

; Requests to TM API
(defn apiGet [client uri] (client/get uri {:headers {"Authorization" (gen-signature client)}}))
(defn apiPut [client uri body] (client/put uri {:headers {"Authorization" (gen-signature client)}
                                                     :content-type :json
                                                     :accept :json
                                                     :body (json/write-str body)
                                                     }))
(defn apiPost [client uri body] (client/post uri {:headers {"Authorization" (gen-signature client)}
                                                     :content-type :json
                                                     :accept :json
                                                     :body (json/write-str body)
                                                     }))
(defn apiDelete [client uri] (client/delete uri {:headers {"Authorization" (gen-signature client)}}))

; Parse JSON string
(def parsejson #(json/read-str %1 :key-fn keyword))

; Get and parse from API
(defn get-optional-data
  "Get value of :data or :results keyword if exists, otherwise return complete map"
  [obj] (:data obj (:results obj obj)))

(def get-and-parse (comp get-optional-data parsejson :body apiGet))
(defn get [client endpoint & args]
  (let [uri (apply gen-uri client endpoint args)]
    (future (get-and-parse client uri))))

;(def future-results (get client :contacts))
;(deref future-results)

;(deref (get client :contacts))
;(deref (get client :contacts 10002))

;(time (get-fut client :contacts))
;(time (deref (get-fut client :contacts)))

(def put-and-parse (comp parsejson :body apiPut))
(defn put [client endpoint id payload & args]
  (let [uri (apply gen-uri client endpoint id args)]
    (future (put-and-parse client uri payload))))

;(deref (put client :contacts 10011 {:email "example999@ticketmatic.nl"}))

(def post-and-parse (comp get-optional-data parsejson :body apiPost))
(defn post [client endpoint payload & args]
  (let [uri (apply gen-uri client endpoint args)]
    (future (post-and-parse client uri payload))))

;(deref (post client :contacts {:email "example@ticketmatic.nl"}))

(defn delete [client endpoint & args]
  (let [uri (apply gen-uri client endpoint args)]
    (future (apiDelete client uri))))

;(deref (delete client :contacts 10013))

; Special Query endpoint
(def query-and-parse (comp get-optional-data parsejson :body apiPost))
(defn query
  "Runs query on TM3 API and returns vector of results"
  [client sql]
  (let [payload {:query sql}
        uri (gen-uri client :queries)]
    (future (query-and-parse client uri payload))))

;(deref (query client "select id from tm.contact limit 10"))

(def export-and-parse (comp #(map parsejson %) clojure.string/split-lines :body apiPost))
(defn export
  "Runs export on TM3 API, processes stream, and returns vector of results"
  [client sql]
  (let [payload {:query sql}
        uri (gen-uri client :export)]
    (future (export-and-parse client uri payload))))

;(deref (export client "select id from tm.contact"))

; Get contacts list
(defn getContacts [client] (get client :contacts))

;(deref (getContacts client))
