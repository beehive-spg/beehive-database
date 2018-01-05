(ns beehive-database.core
  (require [beehive-database.init.schema :as s]
           [datomic.api :as d]
           [liberator.core :as l]
           [ring.middleware.params :as p]
           [compojure.core :as c]
           [beehive-database.queries :as q]
           [ring.middleware.json :as j])
  (:gen-class))




(c/defroutes rest-routes
             (c/GET "/hives" [& ids] (l/resource :available-media-types ["application/json"]
                                                 :handle-ok (q/all-hives)))
             (c/GET "/hives/edges" [] (l/resource :available-media-types
                                                  :handle-ok))
             (c/GET "/hives/workload/:time" [time & ids] (l/resource :available-media-types
                                                                     :handle-ok))
             (c/GET "/hives/reachable/:id1/:id2" [id1 id2] (l/resource))
             (c/GET "/hops" [& ids] (l/resource))
             (c/GET "/routes" [& ids] (l/resource))
             (c/GET "/shops" [& ids] (l/resource))
             (c/POST "/hives" [] (l/resource :allowed-methods [:post]
                                             :available-media-types ["application/json"]
                                             :post! (fn [ctx]
                                                      (q/add-hive "abc" 40.3 42.5 "abcd" '[{:drone/name "accsac" :drone/status :status/IDLE}]))

                                             :handle-ok "ok"))
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
                                          :available-media-types ["application/json"]))
             (c/GET "/refresh" [] (l/resource :allowed-methods [:get]
                                              :available-media-types ["text/html"]
                                              :handle-ok "<html>refreshed</html>")))



(def handler
  (-> rest-routes
      j/wrap-json-body
      j/wrap-json-response
      p/wrap-params))


