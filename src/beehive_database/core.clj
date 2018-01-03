(ns beehive-database.core
  (require [beehive-database.init.schema :as s]
           [datomic.api :as d]
           [liberator.core :as l]
           [ring.middleware.params :as p]
           [compojure.core :as c])
  (:gen-class))


(def uri "datomic:mem://hello")

(d/create-database uri)

(def conn
  (d/connect uri))

(defn init-schema [schema]
  (doseq [i schema]
    @(d/transact conn i)))

(c/defroutes rest-routes
             (c/GET "/hives" [& ids] (l/resource :available-media-types ["application/json"]
                                                 :handle-ok ids))
             (c/GET "/hives/edges" [] (l/resource :available-media-types
                                                  :handle-ok))
             (c/GET "/hives/workload/:time" [time & ids] (l/resource :available-media-types
                                                                     :handle-ok))
             (c/GET "/hives/reachable/:id1/:id2" [id1 id2] (l/resource))
             (c/GET "/hops" [& ids] (l/resource))
             (c/GET "/routes" [& ids] (l/resource))
             (c/GET "/shops" [& ids] (l/resource))
             (c/POST "/routes" [] (l/resource :allowed-methods [:post]
                                              :available-media-types ["application/json"]
                                              :post! (fn [ctx]
                                                       (let [body (slurp (get-in ctx [:request :body]))]
                                                         {::ctx body}))))
             (c/POST "/orders" [] (l/resource :allowed-methods [:post]
                                              :available-media-types ["application/json"]
                                              :post! (fn [ctx])))
             (c/PUT "/routes" [] (l/resource :allowed-methods [:put]
                                             :available-media-types ["application/json"]))
             (c/PUT "/hop" [] (l/resource :allowed-methods [:put]
                                          :available-media-types ["application/json"])))



(def handler
  (-> rest-routes
      p/wrap-params))

(defn init []
  (init-schema s/tables))

(defn destroy []
  (d/delete-database uri))

