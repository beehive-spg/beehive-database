(ns beehive-database.queries
  (require [datomic.api :as d]
           [beehive-database.init.schema :as s]))

(def uri "datomic:mem://hello")

(d/create-database uri)

(def conn
  (d/connect uri))

(defn init-schema [schema]
  (doseq [i schema]
    @(d/transact conn i)))

(init-schema s/tables)

(def db
  (atom (d/db conn)))

(defn refresh []
  (reset! db (d/db conn)))

(defn all-hives []
  (into () (d/q '[:find ?e
                  :where
                  [?e :building/address _]] @db)))

(defn add-hive [address x y name drones]
  @(d/transact conn [{:building/address address
                      :building/xcoord  x
                      :building/ycoord  y}]))

