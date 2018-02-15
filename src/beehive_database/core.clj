(ns beehive-database.core
  (:require [beehive-database.datomic.actions.transactions :as transactions]
            [beehive-database.datomic.actions.data :as data]
            [beehive-database.datomic.init.schema :as schema]
            [datomic-schema.schema :as datomic-schema]
            [datomic.api :as d]
            [ring.adapter.jetty :as jetty]
            [beehive-database.routes :as routes])
  (:gen-class))

(defn- init-schema []
  @(d/transact data/conn (concat
                           (datomic-schema/generate-parts schema/parts)
                           (datomic-schema/generate-schema schema/dbschema))))

(defn- init-data []
  (let [hives (slurp (clojure.java.io/resource "beehive-database/hives.edn"))
        shops (slurp (clojure.java.io/resource "beehive-database/shops.edn"))
        customers (slurp (clojure.java.io/resource "beehive-database/customers.edn"))]
    (transactions/add-drone-type "large" 5000 15 1800 true)
    (doseq [hive (clojure.edn/read-string hives)]
      (transactions/add-hive
        (:building/address hive)
        (:building/xcoord hive)
        (:building/ycoord hive)
        (:hive/name
          (:building/hive hive))))
    (doseq [shop (clojure.edn/read-string shops)]
      (transactions/add-shop
        (:building/address shop)
        (:building/xcoord shop)
        (:building/ycoord shop)
        (:shop/name
          (:building/shop shop))))
    (doseq [customer (clojure.edn/read-string customers)]
      (transactions/add-customer
        (:building/address customer)
        (:building/xcoord customer)
        (:building/ycoord customer)
        (:customer/name
          (:building/customer customer))))))


(defn- init []
  (init-schema)
  (init-data))

(def handler routes/app)

(def port 3000)

(defn -main []
  (init)
  (println (str "Starting server on port " port))
  (jetty/run-jetty handler {:port port}))