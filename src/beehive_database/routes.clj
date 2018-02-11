(ns beehive-database.routes
  (:require [compojure.api.sweet :refer :all]
            [ring.util.http-response :refer :all]
            [schema.core :as s]
            [beehive-database.datomic.actions.queries :as queries]
            [beehive-database.datomic.actions.data :as data]
            [beehive-database.datomic.actions.transactions :as transactions]
            [beehive-database.util :as util]))

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
   :hop/_route   [{:db/id         Long
                   :hop/start     {:db/id Long}
                   :hop/end       {:db/id Long}
                   :hop/starttime Long
                   :hop/endtime   Long}]})

(s/defschema Order
  {:db/id          Long
   :order/shop     Long
   :order/customer Long
   :order/route    Long
   :order/source   {:db/ident s/Keyword}})

(s/defschema Drone
  {:db/id        Long
   :drone/hive   Long
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
      (POST "/" []
        :return Hive
        :body [post-hive PostHive]
        :summary "Saves a hive to the database"
        (let [id (transactions/add-hive (:address post-hive)
                                        (:xcoord post-hive)
                                        (:ycoord post-hive)
                                        (:name post-hive))]
          (created (str "/one/hives/" id) (queries/one :hives id (data/db))))))


    (context "/shops" []
      :tags ["Shops"]
      (GET "/" []
        :query-params [{ids :- [Long] nil}]
        :return [Shop]
        :summary "Returns all/selected shops"
        (ok (queries/all :shops ids (data/db))))
      (POST "/" []
        :return Shop
        :body [post-shop PostShop]
        :summary "Saves a shop to the database"
        (let [id (transactions/add-shop (:address post-shop)
                                        (:xcoord post-shop)
                                        (:ycoord post-shop)
                                        (:name post-shop))]
          (created (str "/one/hives/" id) (queries/one :shops id (data/db))))))


    (context "/customers" []
      :tags ["Customers"]
      (GET "/" []
        :query-params [{ids :- [Long] nil}]
        :return [Customer]
        :summary "Returns all/selected customers"
        (ok (queries/all :customers ids (data/db))))
      (POST "/" []
        :return Customer
        :body [post-customer PostCustomer]
        :summary "Saves a customer to the database"
        (let [id (transactions/add-customer (:address post-customer)
                                            (:xcoord post-customer)
                                            (:ycoord post-customer)
                                            (:name post-customer))]
          (created (str "/one/hives/" id) (queries/one :customers id (data/db))))))


    (context "/routes" []
      :tags ["Routes"]
      (GET "/" []
        :query-params [{ids :- [Long] nil}]
        :return [Route]
        :summary "Returns all/selected routes"
        (ok (queries/all :routes ids (data/db))))
      (POST "/" []
        :return Route
        :body [post-route PostRoute]
        :summary "Saves a route to the database"
        (let [id (transactions/add-route (:hops post-route)
                                         (:origin post-route)
                                         (:time post-route))]
          (created (str "/one/hives/" id) (queries/one :routes id (data/db))))))


    (context "/orders" []
      :tags ["Orders"]
      (GET "/" []
        :query-params [{ids :- [Long] nil}]
        :return [Order]
        :summary "Returns all/selected orders"
        (ok (queries/all :orders ids (data/db))))
      (POST "/" []
        :return Order
        :body [post-order PostOrder]
        :summary "Saves an order to the database"
        (let [id (transactions/add-order (:shopid post-order)
                                         (:customerid post-order)
                                         (:route post-order)
                                         (:source post-order))]
          (created (str "/one/hives/" id) (queries/one :orders id (data/db))))))


    (context "/drones" []
      :tags ["Drones"]
      (GET "/" []
        :query-params [{ids :- [Long] nil}]
        :return [Drone]
        :summary "Returns all/selected drones"
        (ok (queries/all :drones ids (data/db))))
      (POST "/" []
        :return Drone
        :body [post-drone PostDrone]
        :summary "Saves a drone to the database"
        (let [id (transactions/add-drone (:hiveid post-drone)
                                         (:name post-drone)
                                         (:dronetype post-drone)
                                         (:status post-drone))]
          (created (str "/one/hives/" id) (queries/one :drones id (data/db))))))


    (context "/types" []
      :tags ["Types"]
      (GET "/" []
        :query-params [{ids :- [Long] nil}]
        :return [Dronetype]
        :summary "Returns all/selected drone types"
        (ok (queries/all :dronetypes ids (data/db))))
      (POST "/" [Dronetype]
        :return Hive
        :body [post-dronetype PostDronetype]
        :summary "Saves a drone type to the database"
        (let [id (transactions/add-drone-type (:name post-dronetype)
                                              (:range post-dronetype)
                                              (:speed post-dronetype)
                                              (:chargetime post-dronetype)
                                              (:default post-dronetype))]
          (created (str "/one/hives/" id) (queries/one :dronetypes id (data/db))))))


    (context "/one" []
      :tags ["One"]
      (GET "/:table/:id" []
        :path-params [table :- s/Str, id :- Long]
        :summary "Returns the entity with the given id from the given table"
        (ok (queries/one (keyword table) id (data/db)))))

    (context "/api" []
      :tags ["Api"]
      (GET "/delete/:id" []
        :path-params [id :- Long]
        :summary "Deletes entity with specified id"
        (transactions/delete id)
        (no-content))
      (GET "/reachable" []
        :query-params [{ids :- [Long] nil}]
        :summary "Returns all/selected connections"
        (ok (queries/all :connections ids (data/db))))
      (GET "/reachable/:building1/:building2" []
        :path-params [building1 :- Long, building2 :- Long]
        :summary "Returns whether or not the buildings can reach each other using the default drone type"
        :return Boolean
        (ok (queries/is-reachable
              (util/position (queries/one :buildings building1 (data/db)))
              (util/position (queries/one :buildings building2 (data/db)))
              (data/db))))
      (context "/hops" []
        (GET "/incoming/:hiveid/:time" []
          :path-params [hiveid :- Long, time :- Long]
          :summary "Returns incoming hops of specified hive after specified time"
          (ok (queries/incoming-hops-after hiveid time (data/db))))
        (GET "/outgoing/:hiveid/:time" []
          :path-params [hiveid :- Long, time :- Long]
          :summary "Returns outgoing hops of specified hive after specified time"
          (ok (queries/outgoing-hops-after hiveid time (data/db))))))))