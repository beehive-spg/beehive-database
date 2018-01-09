(ns beehive-database.datomic.actions.transactions
  (require [datomic.api :as d]
           [beehive-database.datomic.actions.data :refer :all]
           [beehive-database.datomic.init.schema :as s]
           [beehive-database.datomic.actions.queries :as q]))

(defn add-building [address x y]
  @(d/transact conn
               [{:building/address address
                 :building/xcoord  x
                 :building/ycoord  y}]))

(defn add-hive
  ([address x y name]
   @(d/transact conn
                [{:building/address address
                  :building/xcoord  x
                  :building/ycoord  y
                  :building/hive    {:hive/name name}}]))
  ([buildingid name]
   @(d/transact conn
                [{:db/id         buildingid
                  :building/hive {:hive/name name}}])))

(defn add-shop
  ([address x y name]
   @(d/transact conn
                [{:building/address address
                  :building/xcoord  x
                  :building/ycoord  y
                  :building/shop    {:shop/name name}}]))
  ([buildingid name]
   @(d/transact conn
                [{:db/id         buildingid
                  :building/shop [{:shop/name name}]}])))

(defn add-customer
  ([address x y name]
   @(d/transact conn
                [{:building/address  address
                  :building/xcoord   x
                  :building/ycoord   y
                  :building/customer {:customer/name name}}]))
  ([buildingid name]
   @(d/transact conn
                [{:db/id             buildingid
                  :building/customer [{:customer/name name}]}])))

(defn add-drone [hiveid name status]
  @(d/transact conn
               [{:drone/name   name
                 :drone/status status
                 :drone/hive   hiveid}]))

(defn assign-drone [hiveid droneid]
  @(d/transact conn
               [{:db/id      droneid
                 :drone/hive hiveid}]))

(defn init-schema [schema]
  (doseq [i schema]
    @(d/transact conn i)))

(init-schema s/tables)