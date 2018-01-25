(ns beehive-database.core
  (:require [beehive-database.datomic.actions.queries :as q]
            [beehive-database.datomic.actions.transactions :as t]
            [beehive-database.datomic.actions.data :as d]
            [compojure.core :as c]
            [liberator.core :as l]
            [ring.middleware.params :as p]
            [ring.middleware.json :as j]
            [clojure.data.json :as dj]
            [clojure.spec.alpha :as s])
  (:gen-class))

(defn- extract-json [ctx]
  (dj/read-str
    (slurp (get-in ctx [:request :body]))
    :key-fn keyword))

(defn post-default [post-fn spec]
  {:allowed-methods       [:post]
   :available-media-types ["application/json"]
   :processable?          (fn [ctx]
                            (let [data (extract-json ctx)
                                  valid (s/conform spec data)]
                              (if (= valid ::s/invalid)
                                false
                                {::data data})))
   :post!                 (fn [ctx]
                            (let [data (::data ctx)]
                              (post-fn data)))})

(c/defroutes rest-routes
             (c/GET "/hives" [& ids]
               (l/resource
                 :available-media-types ["application/json"]
                 :handle-ok (q/get-all :hive ids (d/db))))

             (c/GET "/hives/workload/:time" [time & ids]
               (l/resource
                 :available-media-types ["application/json"]
                 :handle-ok))

             (c/GET "/hives/reachable/:id1/:id2" [id1 id2]
               (l/resource
                 :available-media-types ["application/json"]
                 :handle-ok (str
                              (q/is-reachable
                                (read-string id1)
                                (read-string id2)
                                (d/db)))))

             (c/GET "/hops" [& ids]
               (l/resource
                 :available-media-types ["application/json"]
                 :handle-ok (q/get-all :hop ids (d/db))))

             (c/GET "/routes" [& ids]
               (l/resource
                 :available-media-types ["application/json"]
                 :handle-ok (q/get-all :route ids (d/db))))

             (c/GET "/orders" [& ids]
               (l/resource
                 :available-media-types ["application/json"]
                 :handle-ok (q/get-all :order ids (d/db))))

             (c/GET "/predictions" [& ids]
               (l/resource
                 :available-media-types ["application/json"]
                 :handle-ok (q/get-all :prediction ids (d/db))))

             (c/GET "/drones" [& ids]
               (l/resource
                 :available-media-types ["application/json"]
                 :handle-ok (q/get-all :drone ids (d/db))))

             (c/GET "/shops" [& ids]
               (l/resource
                 :available-media-types ["application/json"]
                 :handle-ok (q/get-all :shop ids (d/db))))

             (c/GET "/customers" [& ids]
               (l/resource
                 :available-media-types ["application/json"]
                 :handle-ok (q/get-all :customer ids (d/db))))

             (c/GET "/types" [& ids]
               (l/resource
                 :available-media-types ["application/json"]
                 :handle-ok (q/get-all :dronetype ids (d/db))))

             (c/POST "/hives" []
               (l/resource
                 (post-default
                   #(t/add-hive
                      (:address %)
                      (:xcoord %)
                      (:ycoord %)
                      (:name %))
                   :validation/hive)))

             (c/POST "/drones" []
               (l/resource
                 (post-default
                   #(t/add-drone
                      (:hiveid %)
                      (:name %)
                      (:range %)
                      (:status %))
                   :validation/drone)))

             (c/POST "/routes" []
               (l/resource
                 (post-default
                   #(t/add-route
                      (:hops %)
                      (:origin %))
                   :validation/route)))

             (c/POST "/orders" []
               (l/resource
                 (post-default
                   #(t/add-order
                      (:shopid %)
                      (:customerid %)
                      (:route %))
                   :validation/order)))

             (c/POST "/building" []
               (l/resource
                 (post-default
                   #(t/add-building
                      (:address %)
                      (:xcoord %)
                      (:ycoord %))
                   :validation/building)))

             (c/POST "/shop" []
               (l/resource
                 (post-default
                   #(t/add-shop
                      (:address %)
                      (:xcoord %)
                      (:ycoord %)
                      (:name %))
                   :validation/shop)))

             (c/POST "/customer" []
               (l/resource
                 (post-default
                   #(t/add-customer
                      (:address %)
                      (:xcoord %)
                      (:ycoord %)
                      (:name %))
                   :validation/customer)))

             (c/POST "/hop" []
               (l/resource
                 (post-default
                   #(t/add-hop
                      (:droneid %)
                      (:start %)
                      (:end %))
                   :validation/hop)))

             (c/POST "/dronetype" []
               (l/resource
                 (post-default
                   #(t/add-drone-type
                      (:name %)
                      (:range %)
                      (:speed %)
                      (:chargetime %)
                      (:default %))
                   :validation/ronetype)))

             (c/PUT "/routes" []
               (l/resource
                 :allowed-methods [:put]
                 :available-media-types ["application/json"]))

             (c/PUT "/hop" []
               (l/resource
                 :allowed-methods [:put]
                 :available-media-types ["application/json"]))

             (c/DELETE "/delete/:id" [id]
               :allowed-methods [:delete]
               :available-media-types ["application/json"]
               :delete! (fn [ctx]
                          (t/delete (read-string id)))))


(def handler
  (-> rest-routes
      j/wrap-json-body
      j/wrap-json-response
      p/wrap-params))


