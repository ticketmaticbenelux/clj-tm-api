(defproject clj-tm-api "0.1.0-SNAPSHOT"
  :description "Ticketmatic API wrapper for Clojure"
  :url "https://github.com/ticketmaticbenelux/clj-tm-api"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [clj-http "3.6.1"]
                 [clojure.java-time "0.2.1"]
                 [pandect "0.6.1"]
                 [org.clojure/data.json "0.2.6"]
                 [org.clojure/core.async "0.3.443"]
                 [http-kit "2.2.0"]
                 [http-kit.fake "0.2.1"]])
