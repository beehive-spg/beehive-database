(ns beehive-database.datomic.actions.rules)

(def hive-fields
  [:db/id
   :building/address
   :building/xcoord
   :building/ycoord
   {:building/hive [:db/id
                    :hive/name
                    :hive/demand]}])

(def shop-fields
  [:db/id
   :building/address
   :building/xcoord
   :building/ycoord
   {:building/shop [:db/id
                    :shop/name]}])

(def customer-fields
  [:db/id
   :building/address
   :building/xcoord
   :building/ycoord
   {:building/customer [:db/id
                        :customer/name]}])

(def building-fields
  [:db/id
   :building/address
   :building/xcoord
   :building/ycoord
   {:building/hive [:db/id
                    :hive/name
                    :hive/demand]}
   {:building/customer [:db/id
                        :customer/name]}
   {:building/shop [:db/id
                    :shop/name]}])

(def drone-fields
  [:db/id
   :drone/hive
   :drone/name
   {:drone/type [:db/id
                 :dronetype/name]}
   {:drone/status [:db/ident]}])

(def prediction-fields
  [:db/id
   :prediction/hive
   :prediction/value])

(def hop-fields
  [:db/id
   :hop/start
   :hop/end
   :hop/starttime
   :hop/endtime
   :hop/route
   :hop/distance
   :hop/drone
   :hop/endcharge])

(def route-fields
  [:db/id
   {:route/origin [:db/ident]}
   {:hop/_route [:db/id
                 :hop/start
                 :hop/end
                 :hop/starttime
                 :hop/endtime
                 :hop/distance
                 :hop/drone
                 :hop/endcharge]}])

(def order-fields
  [:db/id
   :order/shop
   :order/customer
   :order/route
   {:order/source [:db/ident]}])

(def connection-fields
  [:db/id
   :connection/start
   :connection/end
   :connection/distance])

(def drone-type-fields
  [:db/id
   :dronetype/name
   :dronetype/range
   :dronetype/speed
   :dronetype/chargetime
   :dronetype/default])

(def fields
  {:hives       hive-fields
   :shops       shop-fields
   :customers   customer-fields
   :buildings   building-fields
   :drones      drone-fields
   :predictions prediction-fields
   :hops        hop-fields
   :routes      route-fields
   :orders      order-fields
   :connections connection-fields
   :dronetypes  drone-type-fields})

(def queries
  {:hives       [:building/hive]
   :shops       [:building/shop]
   :customers   [:building/customer]
   :buildings   [:building/xcoord]
   :drones      [:drone/name]
   :predictions [:prediction]
   :hops        [:hop/start]
   :routes      [:route/origin]
   :orders      [:order/customer]
   :connections [:connection/start]
   :dronetypes  [:dronetype/name]})