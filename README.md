# clj-tm-api

[![Clojars Project](https://img.shields.io/clojars/v/clj-tm-api.svg)](https://clojars.org/clj-tm-api)

A Clojure wrapper for the [Ticketmatic 3 API](https://www.ticketmatic.com/docs/api/).

## Usage

Initialize client:

```clojure
(def client {:account "example"
             :key "xyz"
             :secret "xyz"})
```

### GET request to API

Get contacts, returns Future:

```clojure
(get client :contacts)
```

Use result by dereferencing the Future:

```clojure
(deref (get client :contacts))
```

or:

```clojure
(def future-results (get client :contacts))
(deref future-results)
```

Get contact id's from contact list:

```clojure
(map :id @(getContacts client))
```

Get a specific contact:

```clojure
(get client :contacts 10000)
```

### POST request to API

Create contact:

```clojure
(post client :contacts {:email "example@ticketmatic.com})
```

### PUT request to API

Update contact:

```clojure
(put client :contacts 10000 {:email "example@ticketmatic.com"})
```

### DELETE request to API

Remove contact:

```clojure
(delete client :contacts 10000)
```

### Executing queries

Execute query:

```clojure
(query client "select id from tm.contact limit 5")
```

Execute query with large number of results:

```clojure
(export client "select id from tm.contact")
```

### Supported endpoints

```
:contacts
:contactsimport
:contactsreserve
:relationtypes
:events
:locations
:orders
:import
:reserve
:products
:pricetypes
:pricelists
:orderfeedefinitions
:customfields
:customfieldvalues
:fielddefinitions
:filterdefinitions
:views
:reports
:webskins
:ordermails
:ticketlayouts
:ticketlayouttemplates
:documents
:locktypes
:vouchers
:queries
:export
:eventlocktickets
:eventunlocktickets
:eventseatranks
:ticketsbatch
:logs
:pdf
:tickets
:payments
:confirm
:emailpaymentinstruction
:emaildelivery
:vouchercodes
```

## License

Copyright © 2017 by Ticketmatic Nederland BV (info@ticketmatic.nl).

Distributed under the Eclipse Public License either version 1.0.
