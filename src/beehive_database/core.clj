(ns beehive-database.core
  (require [beehive-database.datomic.actions.queries :as q]
           [beehive-database.datomic.actions.transactions :as t]
           [beehive-database.datomic.actions.data :as d]
           [compojure.core :as c]
           [liberator.core :as l]
           [ring.middleware.params :as p]
           [ring.middleware.json :as j]
           [clojure.data.json :as dj])
  (:gen-class))

(defn- extract-json [ctx]
  (dj/read-str
    (slurp (get-in ctx [:request :body]))
    :key-fn keyword))

(c/defroutes rest-routes
             (c/GET "/hives" [& ids]
               (l/resource
                 :available-media-types ["application/json"]
                 :handle-ok (q/all-hives)))

             (c/GET "/hives/workload/:time" [time & ids]
               (l/resource
                 :available-media-types ["application/json"]
                 :handle-ok))

             (c/GET "/hives/reachable/:id1/:id2" [id1 id2]
               (l/resource
                 :available-media-types ["application/json"]
                 :handle-ok (str (q/is-reachable (read-string id1) (read-string id2)))))

             (c/GET "/hops" [& ids]
               (l/resource))

             (c/GET "/routes" [& ids]
               (l/resource))

             (c/GET "/shops" [& ids]
               (l/resource))

             (c/POST "/hives" []
               (l/resource
                 :allowed-methods [:post]
                 :available-media-types ["application/json"]
                 :processable? (fn [ctx]
                                 (let [data (extract-json ctx)]
                                   {::data data}))
                 :post! (fn [ctx]
                          (let [data (::data ctx)]
                            (t/add-hive
                              (data :address)
                              (data :xcoord)
                              (data :ycoord)
                              (data :name))))))

             (c/POST "/drones" []
               (l/resource
                 :allowed-methods [:post]
                 :available-media-types ["application/json"]
                 :processable? (fn [ctx]
                                 (let [data (extract-json ctx)]
                                   (println data)
                                   {::data data}))
                 :post! (fn [ctx]
                          (let [data (::data ctx)]
                            (t/add-drone
                              (data :hiveid)
                              (data :name)
                              (data :range)
                              (data :status))))))

             (c/POST "/routes" []
               (l/resource
                 :allowed-methods [:post]
                 :available-media-types ["application/json"]
                 :post! (fn [ctx]
                          (let [body (slurp (get-in ctx [:request :body]))]
                            {::ctx body}))))

             (c/POST "/orders" []
               (l/resource
                 :allowed-methods [:post]
                 :available-media-types ["application/json"]
                 :post! (fn [ctx])))

             (c/POST "/assoc" [& ids]
               (l/resource
                 :allowed-methods [:post]
                 :available-media-types ["application/json"]
                 :post! (fn [ctx]
                          (let [data (extract-json ctx)]
                            (t/assign-drone
                              (data :hiveid)
                              (data :droneid))))))

             (c/PUT "/routes" []
               (l/resource
                 :allowed-methods [:put]
                 :available-media-types ["application/json"]))

             (c/PUT "/hop" []
               (l/resource
                 :allowed-methods [:put]
                 :available-media-types ["application/json"]))

             (c/POST "/refresh" []
               (l/resource
                 :allowed-methods [:post]
                 :available-media-types ["text/html"]
                 :post! (fn [ctx]
                          (d/refresh))
                 :handle-ok "<html>refreshed</html>")))

(def handler
  (-> rest-routes
      j/wrap-json-body
      j/wrap-json-response
      p/wrap-params))


