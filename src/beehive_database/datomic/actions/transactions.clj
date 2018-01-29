(ns beehive-database.datomic.actions.transactions
  (:require [datomic.api :as d]
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
   (let [id #db/id[:db.part/user -100]
         tx @(d/transact conn
                         [{:db/id            id
                           :building/address address
                           :building/xcoord  x
                           :building/ycoord  y
                           :building/hive    {:hive/name name}}])
         real-id (d/resolve-tempid (:db-after tx) (:tempids tx) (d/tempid :db.part/user -100))]
     @(d/transact conn [[:connections real-id]])))
  ([buildingid name]
   @(d/transact conn
                [{:db/id         buildingid
                  :building/hive {:hive/name name}}
                 [:connections buildingid]])))

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

(defn add-drone [hiveid name type status]
  @(d/transact conn
               [{:drone/name   name
                 :drone/type   (if (nil? type)
                                 (:db/id (first (first (q/get-default-drone-type (d/db conn)))))
                                 type)
                 :drone/status status
                 :drone/hive   hiveid}]))

(defn add-prediction [value hiveid]
  @(d/transact conn
               [{:prediction/value value
                 :prediction/hive  hiveid}]))

(defn add-hop [drone start end]
  @(d/transact conn
               [{:hop/drone drone
                 :hop/start start
                 :hop/end   end}]))

(defn add-route [hops origin]
  @(d/transact conn
               [{:route/hops   hops
                 :route/origin origin}]))

(defn add-order
  ([shopid customerid routeid]
   @(d/transact conn
                [{:order/shop     shopid
                  :order/customer customerid
                  :order/route    routeid}]))
  ([shopid customerid hops origin]
   @(d/transact conn
                [{:order/shop     shopid
                  :order/customer customerid
                  :order/route    {:route/hops hops}}])))

(defn add-drone-type [name range speed chargetime default]
  @(d/transact conn
               [{:dronetype/name       name
                 :dronetype/range      range
                 :dronetype/speed      speed
                 :dronetype/chargetime chargetime
                 :dronetype/default    default}]))

(defn delete [id]
  @(d/transact conn
               [[:db.fn/retractEntity id]]))

(defn init-schema [schema]
  (doseq [i schema]
    @(d/transact conn i)))

(init-schema s/tables)

(add-drone-type "large" 5000 15 1800 true)

@(d/transact conn
             (read-string (slurp "data.edn")))