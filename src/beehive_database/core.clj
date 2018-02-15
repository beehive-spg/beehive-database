(ns beehive-database.core
  (:require [beehive-database.datomic.actions.transactions :as transactions]
            [beehive-database.datomic.actions.data :as data]
            [beehive-database.datomic.init.schema :as schema]
            [datomic-schema.schema :as datomic-schema]
            [datomic.api :as d]
            [ring.adapter.jetty :as jetty]
            [beehive-database.routes :as routes])
  (:gen-class))

(defn- init-schema []
  @(d/transact data/conn (concat
                           (datomic-schema/generate-parts schema/parts)
                           (datomic-schema/generate-schema schema/dbschema))))

(defn- init-data []
  (let [data (slurp (clojure.java.io/resource "beehive-database/data.edn"))]
    (transactions/add-drone-type "large" 5000 15 1800 true)
    (doseq [hive (clojure.edn/read-string data)]
      (transactions/add-hive
        (:building/address hive)
        (:building/xcoord hive)
        (:building/ycoord hive)
        (:hive/name
          (:building/hive hive))))))

(defn- init []
  (init-schema)
  (init-data))

(def handler routes/app)

(def port 3000)

(defn -main []
  (init)
  (println (str "Starting server on port " port))
  (jetty/run-jetty handler {:port port}))