(ns beehive-database.datomic.actions.queries
  (:require [datomic.api :as d]
            [beehive-database.datomic.init.schema :as s]
            [beehive-database.util :as u]
            [beehive-database.datomic.actions.rules :as r]
            [beehive-database.datomic.actions.data :refer :all]))

(defn all [table ids db]
  (if (empty? ids)
    (d/q '[:find [(pull ?e subquery) ...]
           :in $ subquery [?ref ...]
           :where [?e ?ref _]]
         db
         (get r/fields table)
         (get r/queries table))
    (d/q '[:find [(pull ?e subquery) ...]
           :in $ subquery [?ref ...] [?ids ...]
           :where [?e ?ref _] [?ids]]
         db
         (get r/fields table)
         (get r/queries table)
         ids)))

(defn drones-for-hive [hiveid db]
  (d/q '[:find [(pull ?e subquery) ...]
         :in $ subquery ?hiveid
         :where [?e :drone/hive ?hiveid]]
       db
       (get r/fields :drone)
       hiveid))

(defn one [id db]
  (d/q '[:find (pull ?id [*]) .
         :in $ ?id
         :where [?id]]
       db
       id))

(defn default-drone-type [db]
  (d/q '[:find (pull ?e subquery) .
         :in $ subquery
         :where [?e :dronetype/default true]]
       db (get r/fields :dronetype)))

(defn max-range [db]
  (first
    (first
      (d/q '[:find (max ?e)
             :where
             [_ :dronetype/range ?e]] db))))

(defn is-reachable [p1 p2 db]
  (u/reachable p1 p2 (max-range db)))

(defn reachable [buildingid db]
  (let [buildings (remove
                    #(= (:db/id %) buildingid)
                    (all :hive [] db))
        building (one buildingid db)]
    (filter
      #(is-reachable
         (u/get-pos building)
         (u/get-pos %)
         db)
      buildings)))

(defn get-route [hops time])
