(ns beehive-database.core
  (:require [beehive-database.datomic.actions.queries :as queries]
            [beehive-database.datomic.actions.transactions :as transactions]
            [beehive-database.datomic.actions.data :as data]
            [beehive-database.datomic.init.schema :as schema]
            [beehive-database.datomic.validation.spec :refer :all]
            [datomic-schema.schema :as datomic-schema]
            [datomic.api :as datomic]
            [compojure.core :as compojure]
            [liberator.core :as liberator]
            [ring.middleware.params :as params]
            [ring.middleware.json :as ring-json]
            [clojure.data.json :as data-json]
            [clojure.spec.alpha :as spec]
            [ring.adapter.jetty :as jetty])
  (:gen-class))

(defn json-value-fn [k v]
  (if (clojure.string/starts-with? v ":")
    (keyword (subs v 1))
    v))

(defn- extract-json [ctx]
  (data-json/read-str
    (slurp (get-in ctx [:request :body]))
    :key-fn keyword
    :value-fn json-value-fn))

(defn post-default [post-fn spec redirect-subroute]
  {:allowed-methods       [:post]
   :available-media-types ["application/json"]
   :processable?          (fn [ctx]
                            (let [data (extract-json ctx)
                                  valid (spec/valid? spec data)]
                              (if (not valid)
                                false
                                {::data data})))
   :post!                 (fn [ctx]
                            (let [data (::data ctx)
                                  tx (post-fn data)]
                              {::id (datomic.api/resolve-tempid
                                      (:db-after tx)
                                      (:tempids tx)
                                      (datomic.api/tempid
                                        :db.part/user
                                        -100))}))
   :post-redirect?        (fn [ctx]
                            {:location (str "/one/" redirect-subroute "/" (::id ctx))})})

(compojure/defroutes rest-routes
                     (compojure/GET "/" []
                       (liberator/resource
                         :available-media-types ["text/html"]
                         :handle-ok "<html>We use drones</html>"))

                     (compojure/GET "/one/:subquery/:id" [subquery id]
                       (liberator/resource
                         :available-media-types ["application/json"]
                         :handle-ok (queries/one (keyword (read-string subquery)) (read-string id) (data/db))))

                     (compojure/GET "/hives" [& ids]
                       (liberator/resource
                         :available-media-types ["application/json"]
                         :handle-ok (queries/all :hives ids (data/db))))

                     (compojure/GET "/hives/workload/:time" [time & ids]
                       (liberator/resource
                         :available-media-types ["application/json"]
                         :handle-ok))

                     (compojure/GET "/hives/reachable/:id1/:id2" [id1 id2]
                       (liberator/resource
                         :available-media-types ["application/json"]
                         :handle-ok (str
                                      (queries/is-reachable
                                        (read-string id1)
                                        (read-string id2)
                                        (data/db)))))

                     (compojure/GET "/hops" [& ids]
                       (liberator/resource
                         :available-media-types ["application/json"]
                         :handle-ok (queries/all :hops ids (data/db))))

                     (compojure/GET "/routes" [& ids]
                       (liberator/resource
                         :available-media-types ["application/json"]
                         :handle-ok (queries/all :routes ids (data/db))))

                     (compojure/GET "/routes/distributions/:time1/:time2" [time1 time2]
                       (liberator/resource
                         :available-media-types ["application/json"]
                         :handle-ok (queries/distributions
                                      (read-string time1)
                                      (read-string time2)
                                      (data/db))))

                     (compojure/GET "/orders" [& ids]
                       (liberator/resource
                         :available-media-types ["application/json"]
                         :handle-ok (queries/all :orders ids (data/db))))

                     (compojure/GET "/predictions" [& ids]
                       (liberator/resource
                         :available-media-types ["application/json"]
                         :handle-ok (queries/all :predictions ids (data/db))))

                     (compojure/GET "/drones" [& ids]
                       (liberator/resource
                         :available-media-types ["application/json"]
                         :handle-ok (queries/all :drones ids (data/db))))

                     (compojure/GET "/drones/hive/:id" [id]
                       (liberator/resource
                         :available-media-types ["application/json"]
                         :handle-ok (queries/drones-for-hive
                                      (read-string id)
                                      (data/db))))

                     (compojure/GET "/shops" [& ids]
                       (liberator/resource
                         :available-media-types ["application/json"]
                         :handle-ok (queries/all :shops ids (data/db))))

                     (compojure/GET "/customers" [& ids]
                       (liberator/resource
                         :available-media-types ["application/json"]
                         :handle-ok (queries/all :customers ids (data/db))))

                     (compojure/GET "/types" [& ids]
                       (liberator/resource
                         :available-media-types ["application/json"]
                         :handle-ok (queries/all :dronetype ids (data/db))))

                     (compojure/GET "/reachable" [& ids]
                       (liberator/resource
                         :available-media-types ["application/json"]
                         :handle-ok (queries/all :connections ids (data/db))))

                     (compojure/GET "/incoming/:hiveid/:time" [hiveid time]
                       (liberator/resource
                         :available-media-types ["application/json"]
                         :handle-ok (queries/incoming-hops-after (read-string hiveid) (read-string time) (data/db))))

                     (compojure/GET "/outgoing/:hiveid/:time" [hiveid time]
                       (liberator/resource
                         :available-media-types ["application/json"]
                         :handle-ok (queries/incoming-hops-after (read-string hiveid) (read-string time) (data/db))))

                     (compojure/POST "/hives" []
                       (liberator/resource
                         (post-default
                           #(transactions/add-hive
                              (:address %)
                              (:xcoord %)
                              (:ycoord %)
                              (:name %))
                           :validation/hive
                           "hives")))

                     (compojure/POST "/drones" []
                       (liberator/resource
                         (post-default
                           #(transactions/add-drone
                              (:hiveid %)
                              (:name %)
                              (:dronetype %)
                              (:status %))
                           :validation/drone
                           "drones")))

                     (compojure/POST "/routes" []
                       (liberator/resource
                         (post-default
                           #(transactions/add-route
                              (:hops %)
                              (:origin %)
                              (:time %))
                           :validation/route
                           "routes")))

                     (compojure/POST "/orders" []
                       (liberator/resource
                         (post-default
                           #(transactions/add-order
                              (:shopid %)
                              (:customerid %)
                              (:route %)
                              (:source %))
                           :validation/order
                           "orders")))

                     (compojure/POST "/buildings" []
                       (liberator/resource
                         (post-default
                           #(transactions/add-building
                              (:address %)
                              (:xcoord %)
                              (:ycoord %))
                           :validation/building
                           "buildings")))

                     (compojure/POST "/shops" []
                       (liberator/resource
                         (post-default
                           #(transactions/add-shop
                              (:address %)
                              (:xcoord %)
                              (:ycoord %)
                              (:name %))
                           :validation/shop
                           "shops")))

                     (compojure/POST "/customers" []
                       (liberator/resource
                         (post-default
                           #(transactions/add-customer
                              (:address %)
                              (:xcoord %)
                              (:ycoord %)
                              (:name %))
                           :validation/customer
                           "customers")))

                     (compojure/POST "/hops" []
                       (liberator/resource
                         (post-default
                           #(transactions/add-hop
                              (:droneid %)
                              (:start %)
                              (:end %))
                           :validation/hop
                           "hops")))

                     (compojure/POST "/dronetypes" []
                       (liberator/resource
                         (post-default
                           #(transactions/add-drone-type
                              (:name %)
                              (:range %)
                              (:speed %)
                              (:chargetime %)
                              (:default %))
                           :validation/dronetype
                           "dronetypes")))

                     (compojure/POST "/tryroute" []
                       (liberator/resource
                         (post-default
                           #(queries/route
                              (:hops %)
                              (:time %)
                              (data/db))
                           :validation/tryroute
                           "tryroute")))

                     (compojure/PUT "/routes" []
                       (liberator/resource
                         :allowed-methods [:put]
                         :available-media-types ["application/json"]))

                     (compojure/PUT "/hops" []
                       (liberator/resource
                         :allowed-methods [:put]
                         :available-media-types ["application/json"]))

                     (compojure/PUT "/demand/:hiveid/:demand" [hiveid demand]
                       (liberator/resource
                         :allowed-methods [:put]
                         :available-media-types ["application/json"]
                         :put! (fn [ctx]
                                 (transactions/set-demand (read-string hiveid) (read-string demand)))))

                     (compojure/DELETE "/delete/:id" [id]
                       :allowed-methods [:delete]
                       :available-media-types ["application/json"]
                       :delete! (fn [ctx]
                                  (transactions/delete (read-string id)))))

(defn- init-schema []
  @(datomic/transact data/conn (datomic-schema/generate-schema schema/dbschema)))

(defn- init-data []
  (let [data (slurp (clojure.java.io/resource "beehive-database/data.edn"))]
    (doseq [hive (clojure.edn/read-string data)]
      (transactions/add-hive
        (:building/address hive)
        (:building/xcoord hive)
        (:building/ycoord hive)
        (:hive/name
          (:building/hive hive))))
    (transactions/add-drone-type "large" 5000 15 1800 true)))


(defn- init []
  (init-schema)
  (init-data))

(def handler
  (-> rest-routes
      ring-json/wrap-json-response
      params/wrap-params))

(defn -main []
  (init)
  (jetty/run-jetty handler {:port 3000}))