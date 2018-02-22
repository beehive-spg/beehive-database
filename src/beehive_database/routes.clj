(ns beehive-database.routes
  (:require [compojure.api.sweet :refer :all]
            [ring.util.http-response :refer :all]
            [schema.core :as s]
            [beehive-database.datomic.actions.queries :as queries]
            [beehive-database.datomic.actions.data :as data]
            [beehive-database.datomic.actions.transactions :as transactions]
            [beehive-database.util :as util])
  (:gen-class))

;; Error 400 Schema

(s/defschema ValidationFail
  {:schema   s/Str
   :errors   {}
   :type     s/Str
   :coercion s/Str
   :val      {}
   :in       []})

;; Returned Schemas

(s/defschema Hive
  {:db/id            Long
   :building/address s/Str
   :building/xcoord  Float
   :building/ycoord  Float
   :building/hive    {:db/id       Long
                      :hive/name   s/Str
                      :hive/demand s/Num}})

(s/defschema Shop
  {:db/id            Long
   :building/address s/Str
   :building/xcoord  Float
   :building/ycoord  Float
   :building/shop    [{:db/id     Long
                       :shop/name s/Str}]})

(s/defschema Customer
  {:db/id             Long
   :building/address  s/Str
   :building/xcoord   Float
   :building/ycoord   Float
   :building/customer [{:db/id         Long
                        :customer/name s/Str}]})

(s/defschema Route
  {:db/id        Long
   :route/origin {:db/ident s/Keyword}
   :hop/_route   [{:db/id                          Long
                   :hop/start                      {:db/id Long}
                   :hop/end                        {:db/id Long}
                   :hop/starttime                  Long
                   :hop/endtime                    Long
                   :hop/distance                   Float
                   (s/optional-key :hop/drone)     Long
                   (s/optional-key :hop/endcharge) Float}]})

(s/defschema Order
  {:db/id          Long
   :order/shop     {:db/id Long}
   :order/customer {:db/id Long}
   :order/route    {:db/id Long}
   :order/source   {:db/ident s/Keyword}})

(s/defschema Drone
  {:db/id        Long
   :drone/hive   {:db/id Long}
   :drone/name   s/Str
   :drone/type   {:dronetype/name s/Str}
   :drone/status {:db/ident s/Keyword}})

(s/defschema Dronetype
  {:db/id                Long
   :dronetype/name       s/Str
   :dronetype/range      Long
   :dronetype/speed      Long
   :dronetype/chargetime Long
   :dronetype/default    Boolean})

(s/defschema Cost
  {:db/id     Long
   :hive/cost s/Num})

;; POST input body Schemas

(s/defschema PostHive
  {:address s/Str
   :xcoord  Double
   :ycoord  Double
   :name    s/Str})

(s/defschema PostShop
  {:address s/Str
   :xcoord  Double
   :ycoord  Double
   :name    s/Str})

(s/defschema PostCustomer
  {:address s/Str
   :xcoord  Double
   :ycoord  Double
   :name    s/Str})

(s/defschema PostRoute
  {:hops   [{:from Long :to Long}]
   :origin s/Keyword
   :time   Long})

(s/defschema PostOrder
  {:shopid     Long
   :customerid Long
   :route      Long
   :source     s/Keyword})

(s/defschema PostDrone
  {:hiveid                     Long
   :name                       s/Str
   (s/optional-key :dronetype) Long
   :status                     s/Keyword})

(s/defschema PostDronetype
  {:name       s/Str
   :range      Long
   :speed      Long
   :chargetime Long
   :default    Boolean})

(s/defschema HopEvent
  {:type     s/Str
   :time     Long
   :hop_id   Long
   :route_id Long})

(def app
  (api
    {:swagger
     {:ui   "/"
      :spec "/swagger.json"
      :data {:info {:title       "Drone Logistics Network REST API"
                    :description "Database interface for the Drone Logistics Network project"}
             :tags [{:name "Hives" :description "Hive Endpoints"}
                    {:name "Shops" :description "Shop Endpoints"}
                    {:name "Customers" :description "Customer Endpoints"}
                    {:name "Routes" :description "Route Endpoints"}
                    {:name "Orders" :description "Order Endpoints"}
                    {:name "Drones" :description "Drone Endpoints"}
                    {:name "Types" :description "Dronetype Endpoints"}
                    {:name "One" :description "Single Entity Endpoint"}
                    {:name "Api" :description "API Endpoints"}]}}}

    (context "/hives" []
      :tags ["Hives"]
      (GET "/" []
        :query-params [{ids :- [Long] nil}]
        :return [Hive]
        :summary "Returns all/selected hives"
        (ok (queries/all :hives ids (data/db))))
      (GET "/:id/drones" []
        :path-params [id :- Long]
        :summary "Returns the drones associated with a hive"
        :return [Drone]
        (ok (queries/drones-for-hive id (data/db))))
      (GET "/incoming/:id/:time" []
        :path-params [id :- Long, time :- Long]
        :summary "Returns incoming hops of specified hive after specified time"
        (ok (queries/incoming-hops-after id time (data/db))))
      (GET "/outgoing/:id/:time" []
        :path-params [id :- Long, time :- Long]
        :summary "Returns outgoing hops of specified hive after specified time"
        (ok (queries/outgoing-hops-after id time (data/db))))
      (POST "/" []
        :body [post-hive PostHive]
        :responses {201 {:schema      Hive
                         :description "Hive was created"}}
        :summary "Saves a hive to the database"
        (let [id (transactions/add-hive (:address post-hive)
                                        (:xcoord post-hive)
                                        (:ycoord post-hive)
                                        (:name post-hive))]
          (created (str "/one/hives/" id) (queries/one :hives id (data/db)))))
      (PUT "/:id/:demand" []
        :path-params [id :- Long demand :- Long]
        :return Long
        :summary "Changes a hives demand"
        (ok (transactions/set-demand id demand))))

    (context "/shops" []
      :tags ["Shops"]
      (GET "/" []
        :query-params [{ids :- [Long] nil}]
        :return [Shop]
        :summary "Returns all/selected shops"
        (ok (queries/all :shops ids (data/db))))
      (POST "/" []
        :responses {201 {:schema      Shop
                         :description "Shop was created"}}
        :body [post-shop PostShop]
        :summary "Saves a shop to the database"
        (let [id (transactions/add-shop (:address post-shop)
                                        (:xcoord post-shop)
                                        (:ycoord post-shop)
                                        (:name post-shop))]
          (created (str "/one/shops/" id) (queries/one :shops id (data/db))))))

    (context "/customers" []
      :tags ["Customers"]
      (GET "/" []
        :query-params [{ids :- [Long] nil}]
        :return [Customer]
        :summary "Returns all/selected customers"
        (ok (queries/all :customers ids (data/db))))
      (POST "/" []
        :responses {201 {:schema      Customer
                         :description "Customer was created"}}
        :body [post-customer PostCustomer]
        :summary "Saves a customer to the database"
        (let [id (transactions/add-customer (:address post-customer)
                                            (:xcoord post-customer)
                                            (:ycoord post-customer)
                                            (:name post-customer))]
          (created (str "/one/customers/" id) (queries/one :customers id (data/db))))))

    (context "/routes" []
      :tags ["Routes"]
      (GET "/" []
        :query-params [{ids :- [Long] nil}]
        :return [Route]
        :summary "Returns all/selected routes"
        (ok (queries/all :routes ids (data/db))))
      (POST "/" []
        :responses {201 {:schema      Route
                         :description "Route was created"}}
        :body [post-route PostRoute]
        :summary "Saves a route to the database"
        (let [[id tx] (transactions/add-route (:hops post-route)
                                              (:origin post-route)
                                              (:time post-route))]
          (created (str "/one/routes/" id) (queries/one :routes id (:db-after tx))))))

    (context "/orders" []
      :tags ["Orders"]
      (GET "/" []
        :query-params [{ids :- [Long] nil}]
        :return [Order]
        :summary "Returns all/selected orders"
        (ok (queries/all :orders ids (data/db))))
      (GET "/:routeid" []
        :path-params [routeid :- Long]
        :return Order
        :summary "Returns the order with the specified route"
        (ok (queries/order-with-route routeid (data/db))))
      (POST "/" []
        :responses {201 {:schema      Order
                         :description "Order was created"}}
        :body [post-order PostOrder]
        :summary "Saves an order to the database"
        (let [id (transactions/add-order (:shopid post-order)
                                         (:customerid post-order)
                                         (:route post-order)
                                         (:source post-order))]
          (created (str "/one/orders/" id) (queries/one :orders id (data/db))))))

    (context "/drones" []
      :tags ["Drones"]
      (GET "/" []
        :query-params [{ids :- [Long] nil}]
        :return [Drone]
        :summary "Returns all/selected drones"
        (ok (queries/all :drones ids (data/db))))
      (POST "/" []
        :responses {201 {:schema      Drone
                         :description "Drone was created"}}
        :body [post-drone PostDrone]
        :summary "Saves a drone to the database"
        (let [id (transactions/add-drone (:hiveid post-drone)
                                         (:name post-drone)
                                         (:dronetype post-drone)
                                         (:status post-drone))]
          (created (str "/one/drones/" id) (queries/one :drones id (data/db))))))

    (context "/types" []
      :tags ["Types"]
      (GET "/" []
        :query-params [{ids :- [Long] nil}]
        :return [Dronetype]
        :summary "Returns all/selected drone types"
        (ok (queries/all :dronetypes ids (data/db))))
      (POST "/" []
        :responses {201 {:schema      Dronetype
                         :description "Dronetype was created"}}
        :body [post-dronetype PostDronetype]
        :summary "Saves a drone type to the database"
        (let [id (transactions/add-drone-type (:name post-dronetype)
                                              (:range post-dronetype)
                                              (:speed post-dronetype)
                                              (:chargetime post-dronetype)
                                              (:default post-dronetype))]
          (created (str "/one/types/" id) (queries/one :dronetypes id (data/db))))))

    (context "/one" []
      :tags ["One"]
      (GET "/:table/:id" []
        :path-params [table :- s/Str, id :- Long]
        :summary "Returns the entity with the given id from the given table"
        (ok (queries/one (keyword table) id (data/db)))))

    (context "/api" []
      :tags ["Api"]
      (GET "/delete/:id" []
        :responses {204 {:description "Object deleted, no response content"}}
        :path-params [id :- Long]
        :summary "Deletes entity with specified id"
        (transactions/delete id)
        (no-content))
      (GET "/reachable" []
        :query-params [{ids :- [Long] nil}, {customerid :- Long nil}, {shopid :- Long nil}]
        :summary "Returns all/selected connections. Customer/shop ids need to be the building ids"
        (ok (queries/connections-with-shop-cust ids shopid customerid (data/db))))
      (GET "/reachable/:building1/:building2" []
        :path-params [building1 :- Long, building2 :- Long]
        :summary "Returns whether or not the buildings can reach each other using the default drone type"
        :return Boolean
        (ok (queries/is-reachable
              (util/position (queries/one :buildings building1 (data/db)))
              (util/position (queries/one :buildings building2 (data/db)))
              (data/db))))
      (GET "/distributions/:time1/:time2" []
        :path-params [time1 :- Long, time2 :- Long]
        :summary "Returns all distributions that took place between the specified times"
        :return [Route]
        (ok (queries/distributions time1 time2 (data/db))))
      (GET "/hivecosts" []
        :query-params [ids :- [Long] time :- Long]
        :summary "Returns the cost factor of taking a drone from a selected hive"
        :return [Cost]
        (ok (queries/hivecosts ids time (data/db))))
      (GET "/charge/:droneid/:time" []
        :path-params [droneid :- Long time :- Long]
        (ok (queries/charge-at-time droneid time (data/db))))
      (POST "/tryroute" []
        :responses {201 {:schema      Route
                         :description "Route was created"}}
        :body [post-route PostRoute]
        :summary "Gives data about a route as if it was saved to the database"
        (let [route (transactions/tryroute (:hops post-route)
                                           (:origin post-route)
                                           (:time post-route))]
          (ok route)))
      (POST "/departure" []
        :body [hop-event HopEvent]
        :summary "Called on hop departure. Returns nothing"
        (if (nil? (transactions/departure (:time hop-event)
                                          (:hop_id hop-event)))
          (bad-request)
          (no-content)))
      (POST "/arrival" []
        :body [hop-event HopEvent]
        :summary "Called on hop arrival. Returns nothing"
        (transactions/arrival (:hop_id hop-event))
        (no-content))
      (GET "/tobuilding/:id" []
        :path-params [id :- Long]
        :summary "Returns the building associated with the given hive/customer/shop id"
        (ok (queries/component-to-building id (data/db))))
      (GET "/outgoing/:hiveid/:starttime/:endtime" []
        :path-params [hiveid :- Long starttime :- Long endtime :- Long]
        :return s/Num
        :summary "Returns the number of outgoing hops in a timeframe"
        (ok (queries/outgoing-timeframe starttime endtime hiveid (data/db)))))))



