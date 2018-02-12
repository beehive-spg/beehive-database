(ns beehive-database.datomic.actions.queries
  (:require [datomic.api :as d]
            [beehive-database.util :as util]
            [beehive-database.datomic.actions.rules :as rules]
            [beehive-database.datomic.actions.data :refer :all]))

(defn all [table ids db]
  (if (empty? ids)
    (d/q '[:find [(pull ?e subquery) ...]
           :in $ subquery [?ref ...]
           :where [?e ?ref _]]
         db
         (get rules/fields table)
         (get rules/queries table))
    (d/q '[:find [(pull ?ids subquery) ...]
           :in $ subquery [?ref ...] [?ids ...]
           :where [?ids ?ref _] [?ids]]
         db
         (get rules/fields table)
         (get rules/queries table)
         ids)))

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
       (or (get rules/fields table) '[*])))

(defn default-drone-type [db]
  (d/q '[:find (pull ?e subquery) .
         :in $ subquery
         :where [?e :dronetype/default true]]
       db (get rules/fields :dronetypes)))

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
                       [(< ?starttime ?time2)]))] db time1 time2 (get rules/fields :routes)))

(defn incoming-hops-after [hiveid time db]
  (d/q '[:find [(pull ?hop subquery) ...]
         :in $ ?hiveid ?time subquery
         :where [?hop :hop/end ?hiveid] [?hop :hop/endtime ?endtime] [(< ?time ?endtime)]]
       db
       hiveid
       time
       (get rules/fields :hops)))

(defn outgoing-hops-after [hiveid time db]
  (d/q '[:find [(pull ?hop subquery) ...]
         :in $ ?hiveid ?time subquery
         :where [?hop :hop/start ?hiveid] [?hop :hop/starttime ?starttime] [(< ?time ?starttime)]]
       db
       hiveid
       time
       (get rules/fields :hops)))

(defn order-with-route [routeid db]
  (d/q '[:find (pull ?order subquery) .
         :in $ ?routeid subquery
         :where [?order :order/route ?routeid]
         db
         routeid
         (get rules/fields :orders)]))

(defn is-reachable [p1 p2 db]
  (util/reachable p1 p2 (max-range db)))

(defn reachable [buildingid db]
  (let [buildings (remove
                    #(= (:db/id %) buildingid)
                    (all :hives [] db))
        building (one :hives buildingid db)]
    (filter
      #(is-reachable
         (util/position building)
         (util/position %)
         db)
      buildings)))