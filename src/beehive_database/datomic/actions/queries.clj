(ns beehive-database.datomic.actions.queries
  (require [datomic.api :as d]
           [beehive-database.datomic.init.schema :as s]
           [beehive-database.util :as u]
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
          :in $ [?ids ...]
          :where
          [?e :db/id ?ids]] @db ids)))

(defn get-max-range []
  (first (first (d/q '[:find (max ?e)
                       :where
                       [_ :drone/range ?e]] @db))))

(defn get-reachable [x y]
  (let [hives (all-hives)
        max-range (get-max-range)]
    (map
      #(:db/id (first %))
      (filter
        #(< (u/distance [x y]
                        [(:building/xcoord (first %)) (:building/ycoord (first %))])
            (/ max-range 1000))
        hives))))

