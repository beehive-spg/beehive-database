(ns beehive-database.datomic.actions.queries
  (require [datomic.api :as d]
           [beehive-database.datomic.init.schema :as s]
           [beehive-database.datomic.actions.data :refer :all]))

(defn all-hives [ids]
  (into () (d/q '[:find (pull ?e [*])
                  :in $ [?ids]
                  :where
                  [?e :db/id ?ids]] @db ids)))