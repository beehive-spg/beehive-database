(ns beehive-database.datomic.actions.queries
  (require [datomic.api :as d]
           [beehive-database.datomic.init.schema :as s]
           [beehive-database.datomic.actions.data :refer :all]))

(defn all-hives []
  (into () (d/q '[:find (pull ?e [*])
                  :where
                  [?e :building/address _]] @db)))