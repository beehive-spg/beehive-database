(ns beehive-database.datomic.actions.rules)

(def hive-fields
  [:db/id
   :building/address
   :building/xcoord
   :building/ycoord
   :building/hive])

(def shop-fields
  [:db/id
   :building/address
   :building/xcoord
   :building/ycoord
   :building/shop])

(def customer-fields
  [:db/id
   :building/address
   :building/xcoord
   :building/ycoord
   :building/customer])

(def drone-fields
  [:db/id
   :drone/hive
   :drone/name
   :drone/range
   :drone/status])

(def prediction-fields
  [:db/id
   :prediction/hive
   :prediction/value])

(def hop-fields
  [:db/id
   :hop/drone
   :hop/start
   :hop/end])

(def route-fields
  [:db/id
   :route/origin
   :route/hops])

(def order-fields
  [:db/id
   :order/shop
   :order/customer
   :order/route])