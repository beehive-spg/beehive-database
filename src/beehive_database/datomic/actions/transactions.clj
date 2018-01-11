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

(defn refresh-reachable []
  (let [hives (q/all-hives)]
    @(d/transact conn
                 (mapv #(identity {:db/id          (:db/id (:building/hive (first %)))
                                   :hive/reachable (mapv (fn [x]
                                                           (identity {:db/id x}))
                                                         (q/get-reachable
                                                           (:building/xcoord (first %))
                                                           (:building/ycoord (first %))))})
                       hives))))



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

(defn add-drone [hiveid name range status]
  @(d/transact conn
               [{:drone/name   name
                 :drone/range  range
                 :drone/status status
                 :drone/hive   hiveid}]))

(defn assign-drone [hiveid droneid]
  @(d/transact conn
               [{:db/id      droneid
                 :drone/hive hiveid}]))

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

(defn delete [id]
  @(d/transact conn
               [[:db.fn/retractEntity id]]))

(defn init-schema [schema]
  (doseq [i schema]
    @(d/transact conn i)))

(init-schema s/tables)
(beehive-database.datomic.actions.data/refresh)

