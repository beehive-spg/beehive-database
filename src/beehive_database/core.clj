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
    (transactions/add-drone-type {:name "large" :range 5000 :speed 15 :chargetime 1800 :default true})
    (doseq [hive (clojure.edn/read-string hives)]
      (transactions/add-hive
        {:address (:building/address hive)
         :xcoord  (:building/xcoord hive)
         :ycoord  (:building/ycoord hive)
         :name    (:hive/name
                    (:building/hive hive))}))
    (doseq [shop (clojure.edn/read-string shops)]
      (transactions/add-shop
        {:address (:building/address shop)
         :xcoord  (:building/xcoord shop)
         :ycoord  (:building/ycoord shop)
         :name    (:shop/name
                    (:building/shop shop))}))
    (doseq [customer (clojure.edn/read-string customers)]
      (transactions/add-customer
        {:address (:building/address customer)
         :xcoord  (:building/xcoord customer)
         :ycoord  (:building/ycoord customer)
         :name    (:customer/name
                    (:building/customer customer))}))))


(defn- init []
  (init-schema)
  (init-data))

(def handler routes/app)

(def port 3000)

(defn -main []
  (init)
  (println (str "Starting server on port " port))
  (jetty/run-jetty handler {:port port}))