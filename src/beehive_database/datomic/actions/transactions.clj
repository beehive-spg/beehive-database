(ns beehive-database.datomic.actions.transactions
  (:require [datomic.api :as d]
            [beehive-database.datomic.actions.data :refer :all]
            [beehive-database.datomic.actions.queries :as queries]))

(defn transact->id [conn data]
  (let [id (d/tempid :db.part/user)
        tx @(d/transact conn [(assoc (first data) :db/id id)])
        db-after (:db-after tx)
        tempids (:tempids tx)
        real-id (d/resolve-tempid db-after tempids id)]
    real-id))


(defn add-building [address x y]
  (let [id (transact->id conn
                         [{:building/address address
                           :building/xcoord  x
                           :building/ycoord  y}])]
    id))

(defn add-hive [address x y name]
  (let [id (transact->id conn [{:building/address address
                                :building/xcoord  x
                                :building/ycoord  y
                                :building/hive    {:hive/name   name
                                                   :hive/demand -1}}])]
    @(d/transact conn (queries/connections (db) id))
    id))

(defn add-shop [address x y name]
  (let [id (transact->id conn
                         [{:building/address address
                           :building/xcoord  x
                           :building/ycoord  y
                           :building/shop    {:shop/name name}}])]
    id))


(defn add-customer [address x y name]
  (let [id (transact->id conn
                         [{:building/address  address
                           :building/xcoord   x
                           :building/ycoord   y
                           :building/customer {:customer/name name}}])]
    id))

(defn add-drone [hiveid name type status]
  (let [id (transact->id conn
                         [{:drone/name   name
                           :drone/type   (if (nil? type)
                                           (:db/id (queries/default-drone-type (d/db conn)))
                                           type)
                           :drone/status status
                           :drone/hive   hiveid}])]
    id))

(defn add-route [hops origin time]
  (let [id (transact->id conn [{:route/origin origin}])]
    @(d/transact conn (queries/mkroute (db) hops id time))
    id))


(defn add-order
  [shopid customerid routeid source]
  (let [id (transact->id conn
                         [{:order/shop     shopid
                           :order/customer customerid
                           :order/route    routeid
                           :order/source   source}])]
    id))

(defn add-drone-type [name range speed chargetime default]
  (let [id (transact->id conn
                         [{:dronetype/name       name
                           :dronetype/range      range
                           :dronetype/speed      speed
                           :dronetype/chargetime chargetime
                           :dronetype/default    default}])]
    id))

(defn delete [id]
  @(d/transact conn
               [[:db.fn/retractEntity id]]))

(defn set-demand [hiveid demand]
  @(d/transact conn
               [{:db/id       hiveid
                 :hive/demand demand}]))