(ns beehive-database.datomic.actions.transactions
  (:require [datomic.api :as datomic]
            [beehive-database.datomic.actions.data :refer :all]
            [beehive-database.datomic.init.schema :as schema]
            [beehive-database.datomic.actions.queries :as queries]))

(defn transact [conn data]
  (let [id #db/id[:db.part/user -100]]
    @(datomic/transact conn [(assoc (first data) :db/id id)])))


(defn add-building [address x y]
  (transact conn
            [{:building/address address
              :building/xcoord  x
              :building/ycoord  y}]))

(defn add-hive
  ([address x y name]
   (let [tx (transact conn
                      [{:building/address address
                        :building/xcoord  x
                        :building/ycoord  y
                        :building/hive    {:hive/name   name
                                           :hive/demand -1}}])
         real-id (datomic/resolve-tempid (:db-after tx) (:tempids tx) (datomic/tempid :db.part/user -100))
         tx2 @(datomic/transact conn [[:connections real-id]])]
     tx))

  ([buildingid name]
   @(datomic/transact conn
                      [{:db/id         buildingid
                        :building/hive {:hive/name name}}
                       [:connections buildingid]])))

(defn add-shop
  ([address x y name]
   (transact conn
             [{:building/address address
               :building/xcoord  x
               :building/ycoord  y
               :building/shop    {:shop/name name}}]))
  ([buildingid name]
   @(datomic/transact conn
                      [{:db/id         buildingid
                        :building/shop [{:shop/name name}]}])))


(defn add-customer
  ([address x y name]
   (transact conn
             [{:building/address  address
               :building/xcoord   x
               :building/ycoord   y
               :building/customer {:customer/name name}}]))
  ([buildingid name]
   @(datomic/transact conn
                      [{:db/id             buildingid
                        :building/customer [{:customer/name name}]}])))

(defn add-drone [hiveid name type status]
  (transact conn
            [{:drone/name   name
              :drone/type   (if (nil? type)
                              (:db/id (queries/default-drone-type (datomic/db conn)))
                              type)
              :drone/status status
              :drone/hive   hiveid}]))

(defn add-prediction [value hiveid]
  (transact conn
            [{:prediction/value value
              :prediction/hive  hiveid}]))

(defn add-hop [drone start end]
  (transact conn
            [{:hop/drone drone
              :hop/start start
              :hop/end   end}]))

(defn add-route [hops origin time]
  (let [tx (transact conn [{:route/origin origin}])
        real-id (datomic/resolve-tempid (:db-after tx) (:tempids tx) (datomic/tempid :db.part/user -100))]
    (do @(datomic/transact conn [[:mkroute hops real-id time]])
        tx)))


(defn add-order
  [shopid customerid routeid source]
  (transact conn
            [{:order/shop     shopid
              :order/customer customerid
              :order/route    routeid
              :order/source   source}]))

(defn add-drone-type [name range speed chargetime default]
  (transact conn
            [{:dronetype/name       name
              :dronetype/range      range
              :dronetype/speed      speed
              :dronetype/chargetime chargetime
              :dronetype/default    default}]))

(defn delete [id]
  @(transact conn
             [[:db.fn/retractEntity id]]))

(defn set-demand [hiveid demand]
  @(datomic/transact conn
                     [{:db/id       hiveid
                       :hive/demand demand}]))