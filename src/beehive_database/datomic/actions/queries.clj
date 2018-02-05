(ns beehive-database.datomic.actions.queries
  (:require [datomic.api :as d]
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
    (d/q '[:find [(pull ?ids subquery) ...]
           :in $ subquery [?ref ...] [?ids ...]
           :where [?ids ?ref _] [?ids]]
         db
         (get r/fields table)
         (get r/queries table)
         (mapv
           read-string
           (vals ids)))))

(defn drones-for-hive [hiveid db]
  (d/q '[:find [(pull ?e [:db/id
                          :drone/name
                          :drone/type
                          :drone/status]) ...]
         :in $ ?hiveid
         :where [?e :drone/hive ?hiveid]]
       db
       hiveid))

(defn hops-for-drones [droneids db]
  (d/q '[:find [(pull ?hops [*]) ...]
         :in $ [?droneids ...]
         :where [?hops :hop/drone ?droneids]]
       db
       droneids))

(defn one [table id db]
  (d/q '[:find (pull ?id subquery) .
         :in $ ?id subquery
         :where [?id]]
       db
       id
       (or (get r/fields table) '[*])))

(defn default-drone-type [db]
  (d/q '[:find (pull ?e subquery) .
         :in $ subquery
         :where [?e :dronetype/default true]]
       db (get r/fields :dronetypes)))

(defn max-range [db]
  (d/q '[:find (max ?e) .
         :where
         [_ :dronetype/range ?e]] db))

(defn distributions [time1 time2 db]
  (d/q '[:find [(pull ?route subquery) ...]
         :in $ ?time1 ?time2 subquery
         :where
         [?route :route/origin :origin/DISTRIBUTION]
         [?hop :hop/route ?route]
         [?hop :hop/starttime ?starttime]
         (or-join [?starttime ?time1 ?time2]
                  (and [(> ?starttime ?time1)]
                       [(< ?starttime ?time2)]))] db time1 time2 (get r/fields :routes)))

(defn is-reachable [p1 p2 db]
  (u/reachable p1 p2 (max-range db)))

(defn reachable [buildingid db]
  (let [buildings (remove
                    #(= (:db/id %) buildingid)
                    (all :hives [] db))
        building (one :hives buildingid db)]
    (filter
      #(is-reachable
         (u/position building)
         (u/position %)
         db)
      buildings)))

(defn route [hops time db])

(defn available-drones [hiveid time db]                     ;;not tested
  (let [drones (drones-for-hive hiveid db)
        droneids (map #(:db/id %) drones)
        hops (hops-for-drones droneids db)]
    (-
      (count drones)
      (count
        (distinct
          (map
            #(:hop/drone %)
            (filter
              #(=
                 -1
                 (compare
                   time
                   (:hop/endtime %))
                 hops))))))))

(defn workload [hiveid time db]                             ;;not tested
  (let [maxdrones (count (drones-for-hive hiveid db))
        available (available-drones hiveid time db)
        workload (*
                   100
                   (/
                     (-
                       maxdrones
                       available)
                     maxdrones))]
    workload))
