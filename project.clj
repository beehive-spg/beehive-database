(defproject beehive-database "0.1.0-SNAPSHOT"
  :description "Database component of Drone Logistics Network Diploma Project at HTL Spengergasse"
  :url "spengergasse.at"
  :license {:name "Apache License 2.0"
            :url  "http://www.apache.org/licenses/"}
  :repositories {"my.datomic.com" {:url      "https://my.datomic.com/repo"
                                   :username :env/USERNAME
                                   :password :env/PASSWORD}}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [com.datomic/datomic-pro "0.9.5656"]
                 [ring/ring-core "1.6.3"]
                 [ring/ring-jetty-adapter "1.6.3"]
                 [datomic-schema "1.3.0"]
                 [metosin/compojure-api "2.0.0-SNAPSHOT"]]
  :profiles {:uberjar {:aot  :all
                       :main beehive-database.core}}
  :main beehive-database.core)


