(ns beehive-database.datomic.actions.queries
  (:require [datomic.api :as d]
            [beehive-database.datomic.init.schema :as s]
            [beehive-database.util :as u]
            [beehive-database.datomic.actions.rules :as r]
            [beehive-database.datomic.actions.data :refer :all]))

(defn get-all [table ids db]
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

(defn get-one [id db]
  (d/q '[:find (pull ?id [*]) .
         :in $ ?id
         :where [?id]]
       db
       id))

(defn get-default-drone-type [db]
  (d/q '[:find (pull ?e subquery) .
         :in $ subquery
         :where [?e :dronetype/default true]]
       db (get r/fields :dronetype)))

(defn get-max-range [db]
  (first
    (first
      (d/q '[:find (max ?e)
             :where
             [_ :dronetype/range ?e]] db))))

(defn is-reachable [p1 p2 db]
  (u/reachable p1 p2 (get-max-range db)))

(defn get-reachable [buildingid db]
  (let [buildings (remove
                    #(= (:db/id %) buildingid)
                    (get-all :hive [] db))
        building (get-one buildingid db)]
    (filter
      #(is-reachable
         (u/get-pos building)
         (u/get-pos %)
         db)
      buildings)))

(defn get-route [hops time])
