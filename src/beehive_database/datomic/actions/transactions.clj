(ns beehive-database.datomic.actions.transactions
  (require [datomic.api :as d]
           [beehive-database.datomic.actions.data :refer :all]
           [beehive-database.datomic.init.schema :as s]))

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

(defn assign-drone [hiveid droneid]
  @(d/transact conn [[:db/add
                      hiveid
                      :hive/drones
                      droneid]]))

(defn init-schema [schema]
  (doseq [i schema]
    @(d/transact conn i)))

(init-schema s/tables)