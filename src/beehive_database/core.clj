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
               (l/resource
                 :available-media-types ["application/json"]
                 :handle-ok (q/all-hops)))

             (c/GET "/routes" [& ids]
               (l/resource
                 :available-media-types ["application/json"]
                 :handle-ok (q/all-routes)))

             (c/GET "/orders" [& ids]
               (l/resource
                 :available-media-types ["application/json"]
                 :handle-ok (q/all-orders)))

             (c/GET "/predictions" [& ids]
               (l/resource
                 :available-media-types ["application/json"]
                 :handle-ok (q/all-predictions)))

             (c/GET "/drones" [& ids]
               (l/resource
                 :available-media-types ["application/json"]
                 :handle-ok (q/all-drones)))

             (c/GET "/shops" [& ids]
               (l/resource
                 :available-media-types ["application/json"]
                 :handle-ok (q/all-shops)))

             (c/GET "/customers" [& ids]
               (l/resource
                 :available-media-types ["application/json"]
                 :handle-ok (q/all-customers)))

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
                 :handle-ok "<html>refreshed</html>"))

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


