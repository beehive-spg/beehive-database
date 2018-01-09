(ns beehive-database.datomic.actions.queries
  (require [datomic.api :as d]
           [beehive-database.datomic.init.schema :as s]
           [beehive-database.datomic.actions.data :refer :all]))

(defn all-hives
  ([]
   (d/q '[:find (pull ?e [:db/id
                          :building/address
                          :building/xcoord
                          :building/ycoord
                          :building/hive])
          :where
          [?e :building/hive _]] @db))
  ([ids]
   (d/q '[:find (pull ?e [:db/id
                          :building/address
                          :building/xcoord
                          :building/ycoord
                          :building/hive])
          :in $ [?ids]
          :where
          [?e :db/id ?ids]] @db ids)))

