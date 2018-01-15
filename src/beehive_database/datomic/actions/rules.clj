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
   {:drone/status [:db/ident]}])

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
   {:route/origin [:db/ident]}
   :route/hops])

(def order-fields
  [:db/id
   :order/shop
   :order/customer
   :order/route])

(def connection-fields
  [:db/id
   :connection/start
   :connection/end
   :connection/distance])

(def fields
  {:hive       hive-fields
   :shop       shop-fields
   :customer   customer-fields
   :drone      drone-fields
   :prediction prediction-fields
   :hop        hop-fields
   :route      route-fields
   :order      order-fields
   :connection connection-fields})

(def queries
  {:hive       [:building/hive]
   :shop       [:building/shop]
   :customer   [:building/customer]
   :drone      [:drone/name]
   :prediction [:prediction]
   :hop        [:hop/start]
   :route      [:route/origin]
   :order      [:order/customer]
   :connection [:connection/start]})