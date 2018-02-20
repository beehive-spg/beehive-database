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
         [?route :route/origin :route.origin/distribution]
         [?hop :hop/route ?route]
         [?hop :hop/starttime ?starttime]
         (or-join [?starttime ?time1 ?time2]
                  (and [(> ?starttime ?time1)]
                       [(< ?starttime ?time2)]))] db time1 time2 (get rules/fields :routes)))

(defn incoming-hops-after [hiveid time db]
  (d/q '[:find [(pull ?hop subquery) ...]
         :in $ ?hiveid ?time subquery
         :where [(missing? $ ?hop :hop/drone)] [?hop :hop/end ?hiveid] [?hop :hop/endtime ?endtime] [(< ?time ?endtime)]]
       db
       hiveid
       time
       (get rules/fields :hops)))

(defn outgoing-hops-after [hiveid time db]
  (d/q '[:find [(pull ?hop subquery) ...]
         :in $ ?hiveid ?time subquery
         :where [(missing? $ ?hop :hop/drone)] [?hop :hop/start ?hiveid] [?hop :hop/starttime ?starttime] [(< ?time ?starttime)]]
       db
       hiveid
       time
       (get rules/fields :hops)))

(defn incoming-hops-until [hiveid time db]
  (d/q '[:find [(pull ?hop subquery) ...]
         :in $ ?hiveid ?time subquery
         :where [(missing? $ ?hop :hop/drone)] [?hop :hop/start ?hiveid] [?hop :hop/starttime ?starttime] [(> ?time ?starttime)]]
       db
       hiveid
       time
       (get rules/fields :hops)))


(defn order-with-route [routeid db]
  (d/q '[:find (pull ?order subquery) .
         :in $ ?routeid subquery
         :where [?order :order/route ?routeid]]
       db
       routeid
       (get rules/fields :orders)))

(defn conns [hiveids db]
  (if (nil? hiveids)
    (all :connections hiveids db)
    (d/q '[:find [(pull ?conn subquery) ...]
           :in $ [?hiveids ...] subquery
           :where (or-join [?conn ?hiveids]
                           [?conn :connection/start ?hiveids]
                           [?conn :connection/end ?hiveids])]
         db
         hiveids
         (get rules/fields :connections))))


(defn is-reachable [p1 p2 db]
  (util/reachable p1 p2 (max-range db)))

(defn reachable [buildingid db]
  (let [buildings (remove
                    #(= (:db/id %) buildingid)
                    (all :hives [] db))
        building (one :buildings buildingid db)]
    (filter
      #(is-reachable
         (util/position building)
         (util/position %)
         db)
      buildings)))

(defn connections [db buildingid]
  (mapv
    (fn [building]
      {:db/id               (d/tempid :db.part/user)
       :connection/start    buildingid
       :connection/end      (:db/id building)
       :connection/distance (util/distance
                              (util/position (one :buildings buildingid db))
                              (util/position building))})
    (reachable buildingid db)))

(defn mkroute [db hops routeid time]
  (let [speed (:dronetype/speed (default-drone-type db))]
    (loop [result []
           lhops hops
           starttime time]
      (if (empty? lhops)
        result
        (let [endtime (long (+ starttime
                               (* 1000
                                  (util/travel-time
                                    (util/position (one :buildings (:from (first lhops)) db))
                                    (util/position (one :buildings (:to (first lhops)) db))
                                    speed))))]
          (recur (conj result {:hop/route     routeid
                               :hop/start     (:from (first lhops))
                               :hop/end       (:to (first lhops))
                               :hop/starttime starttime
                               :hop/endtime   endtime})
                 (drop 1 lhops)
                 (+ endtime 3000)))))))

(defn connections-with-shop-cust [hiveids shopid custid db]
  (let [db-after-shop (if (nil? shopid)
                        db
                        (:db-after (d/with db (connections db shopid))))
        db-after-cust (if (nil? custid)
                        db-after-shop
                        (:db-after (d/with db-after-shop (connections shopid db-after-shop))))]
    (concat (conns hiveids db) (conns [shopid] db-after-shop) (conns [custid] db-after-cust))))


(defn hivecosts [hiveids time db]
  (mapv
    (fn [hive]
      (let [hiveid (:db/id hive)
            demand (:hive/demand (:building/hive hive))
            drones (drones-for-hive hiveid db)
            numdrones (count drones)
            outgoing-now (outgoing-hops-after hiveid (.getTime (java.util.Date.)) db)
            incoming-until-time (incoming-hops-until hiveid time db)
            drones-at-time (+ (- numdrones (count outgoing-now)) (count incoming-until-time))
            free-at-time (- drones-at-time demand)
            percent-takeout (if (= 0 drones-at-time)
                              100000
                              (* 100 (/ 1 drones-at-time)))
            cost (if (< free-at-time 1)
                   (* 3 percent-takeout)
                   percent-takeout)
            mapped-cost (if (= demand -1)
                          1
                          (util/map-num cost 0 300 1 20))]
        {:db/id     hiveid
         :hive/cost mapped-cost}))
    (all :hives hiveids db)))