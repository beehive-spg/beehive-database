(ns beehive-database.datomic.actions.queries
  (require [datomic.api :as d]
           [beehive-database.datomic.init.schema :as s]
           [beehive-database.util :as u]
           [beehive-database.datomic.actions.rules :as r]
           [beehive-database.datomic.actions.data :refer :all]))

(defn get-all [table ids]
  (if (empty? ids)
    (d/q '[:find  (pull ?e subquery)
           :in    $ subquery [?ref ...]
           :where [?e ?ref _]] @db (get r/fields table) (get r/queries table))
    (d/q '[:find (pull ?e subquery)
           :in $ subquery [?ref ...] [?ids ...]
           :where [?e ?ref ?ids]] @db (get r/fields table) (get r/queries table) ids)))

(defn get-max-range []
  (first (first (d/q '[:find (max ?e)
                       :where
                       [_ :drone/range ?e]] @db))))

(defn get-reachable [x y]
  (let [hives (get-all :hive [])
        max-range (get-max-range)]
    (map
      #(:db/id (first %))
      (filter
        #(< (u/distance [x y]
                        [(:building/xcoord (first %)) (:building/ycoord (first %))])
            (/ max-range 1000))
        hives))))

(defn is-reachable [id1 id2]
  (let [[hive1 hive2] (get-all :hive [id1 id2])]
    (u/reachable
      [(:building/xcoord (first hive1)) (:building/ycoord (first hive2))]
      [(:building/xcoord (first hive2)) (:building/ycoord (first hive2))]
      (get-max-range))))
