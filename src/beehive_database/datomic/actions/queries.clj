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

(defn all-ids [table db]
  (d/q '[:find [?id ...]
         :in $ [?ref ...]
         :where [?id ?ref _]]
       db
       (get rules/queries table)))

(defn drones-for-hive [hiveid db]
  (d/q '[:find [(pull ?e subquery) ...]
         :in $ ?hiveid subquery
         :where [?e :drone/hive ?hiveid]]
       db
       hiveid
       (get rules/fields :drones)))

(defn drone-ids [buildingid db]
  (d/q '[:find ?id
         :in $ ?buildingid
         :where [?id :drone/hive ?buildingid]]
       db
       buildingid))

(defn hops-for-drone [droneid db]
  (d/q '[:find [(pull ?hops subquery ...)]
         :in $ ?droneid subquery
         :where [?hops :hop/drone ?droneid]]
       db
       droneid
       (get rules/fields :hops)))

(defn hop-ids [droneid db]
  (d/q '[:find [?id ...]
         :in $ ?droneid
         :where [?id :hop/drone ?droneid]]
       db
       droneid))

(defn one [table id db]
  (d/q '[:find (pull ?id subquery) .
         :in $ ?id subquery
         :where [?id]]
       db
       id
       (get rules/fields table)))

(defn component-to-building [id db]
  (d/q '[:find [(pull ?building subquery) ...]
         :in $ ?id subquery
         :where (or-join [?building ?id]
                         [?building :building/hive ?id]
                         [?building :building/customer ?id]
                         [?building :building/shop ?id])]
       db
       id
       (get rules/fields :buildings)))

(defn default-drone-type [db]
  (d/q '[:find (pull ?e subquery) .
         :in $ subquery
         :where [?e :dronetype/default true]]
       db (get rules/fields :dronetypes)))

(defn default-drone-type-id [db]
  (d/q '[:find ?id .
         :in $
         :where [?id :dronetype/default true]]
       db))

(defn max-range [db]
  (d/q '[:find (max ?e) .
         :where [_ :dronetype/range ?e]]
       db))

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

(defn incoming-hop-ids-after [hiveid time db]
  (d/q '[:find [?id ...]
         :in $ ?hiveid ?time
         :where [?id :hop/end ?hiveid] [?id :hop/endtime ?endtime] [(< ?time ?endtime)]]
       db
       hiveid
       time))

(defn outgoing-hops-after [hiveid time db]
  (d/q '[:find [(pull ?hop subquery) ...]
         :in $ ?hiveid ?time subquery
         :where [(missing? $ ?hop :hop/drone)] [?hop :hop/start ?hiveid] [?hop :hop/starttime ?starttime] [(< ?time ?starttime)]]
       db
       hiveid
       time
       (get rules/fields :hops)))

(defn outgoing-hop-ids-after [hiveid time db]
  (d/q '[:find [?id ...]
         :in $ ?hiveid ?time
         :where [?id :hop/start ?hiveid] [?id :hop/starttime ?starttime] [(< ?time ?starttime)]]
       db
       hiveid
       time))

(defn incoming-hops-until [hiveid time db]
  (d/q '[:find [(pull ?hop subquery) ...]
         :in $ ?hiveid ?time subquery
         :where [(missing? $ ?hop :hop/drone)] [?hop :hop/end ?hiveid] [?hop :hop/starttime ?starttime] [(> ?time ?starttime)]]
       db
       hiveid
       time
       (get rules/fields :hops)))

(defn incoming-hop-ids-until [hiveid time db]
  (d/q '[:find [?id ...]
         :in $ ?hiveid ?time
         :where [?id :hop/end ?hiveid] [?id :hop/starttime ?starttime] [(> ?time ?starttime)]]
       db
       hiveid
       time))

(defn incoming-hops-timeframe [hiveid starttimeframe endtimeframe db]
  (d/q '[:find [(pull ?hop subquery) ...]
         :in $ ?hiveid ?stattimeframe ?endtimeframe subquery
         :where [?hop :hop/end ?hiveid] [?hop :hop/endtime ?endtime] [(< ?endtime ?endtimeframe)] [(> ?endtime ?stattimeframe)]]
       db
       hiveid
       starttimeframe
       endtimeframe
       (get rules/fields :hops)))

(defn incoming-hop-ids-timeframe [hiveid start end db]
  (d/q '[:find [?id ...]
         :in $ ?hiveid ?start ?end
         :where [?id :hop/end ?hiveid] [?hop :hop/endtime ?endtime] [(< ?endtime ?end)] [(> ?endtime ?start)]]
       db
       hiveid
       start
       end))

(defn outgoing-hops-timeframe [hiveid starttimeframe endtimeframe db]
  (d/q '[:find [(pull ?hop subquery) ...]
         :in $ ?hiveid ?stattimeframe ?endtimeframe subquery
         :where [?hop :hop/start ?hiveid] [?hop :hop/starttime ?starttime] [(< ?starttime ?endtimeframe)] [(> ?starttime ?stattimeframe)]]
       db
       hiveid
       starttimeframe
       endtimeframe
       (get rules/fields :hops)))

(defn outgoing-hop-ids-timeframe [hiveid start end db]
  (d/q '[:find [?id ...]
         :in $ ?hiveid ?start ?end
         :where [?id :hop/start ?hiveid] [?hop :hop/starttime ?starttime] [(< ?starttime ?end)] [(> ?starttime ?start)]]
       db
       hiveid
       start
       end))

(defn order-with-route [routeid db]
  (d/q '[:find (pull ?order subquery) .
         :in $ ?routeid subquery
         :where [?order :order/route ?routeid]]
       db
       routeid
       (get rules/fields :orders)))

(defn order-id [routeid db]
  (d/q '[:find ?id .
         :in $ ?routeid
         :where [?id :order/route ?routeid]]
       db
       routeid))

(defn saved-connections [hiveids db]
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

(defn connection-ids
  ([db]
   (all-ids :connections db))
  ([hiveids db]
   (d/q '[:find [?id ...]
          :in $ [?hiveids ...]
          :where (or-join [?id ?hiveids]
                          [?id :connection/start ?hiveids]
                          [?id :connection/end ?hiveids])]
        db
        hiveids)))

(defn drone-speed [droneid db]
  (d/q '[:find ?speed .
         :in $ ?droneid
         :where [?droneid :drone/type ?dronetype] [?dronetype :dronetype/speed ?speed]]
       db
       droneid))

(defn is-reachable [p1 p2 db]
  (util/reachable p1 p2 (max-range db)))

(defn building-position [buildingid db]
  (let [{x :building/xcoord
         y :building/ycoord} (d/pull db [:building/xcoord :building/ycoord] buildingid)]
    [x y]))

(defn distance [buildingid1 buildingid2 db]
  (util/distance (building-position buildingid1 db)
                 (building-position buildingid2 db)))

(defn travel-time [buildingid1 buildingid2 droneid db]
  (util/travel-time (building-position buildingid1 db)
                    (building-position buildingid2 db)
                    (drone-speed droneid db)))

(defn is-reachable2 [buildingid1 buildingid2 db]
  (util/reachable (building-position buildingid1 db)
                  (building-position buildingid2 db)
                  (max-range db)))

(defn reachable [buildingid db extra-buildingids]
  (let [buildings (remove
                    #(= (:db/id %) buildingid)
                    (all :hives [] db))
        buildings (if (empty? extra-buildingids)
                    buildings
                    (concat buildings (all :buildings extra-buildingids db)))
        building (one :buildings buildingid db)]
    (filter
      #(is-reachable
         (util/position building)
         (util/position %)
         db)
      buildings)))

(defn reachable-ids [buildingid db extra-buildingids]
  (->> (all-ids :hives db)
       (concat (d/pull-many db [:db/id] extra-buildingids))
       (filter #(is-reachable2 buildingid % db))))

(defn gen-connections [db buildingid & extra-buildingids]
  (mapv
    (fn [building]
      {:db/id               (d/tempid :db.part/user)
       :connection/start    buildingid
       :connection/end      (:db/id building)
       :connection/distance (util/distance
                              (util/position (one :buildings buildingid db))
                              (util/position building))})
    (reachable buildingid db extra-buildingids)))

(defn mkroute [db hops routeid time]
  (let [speed (:dronetype/speed (default-drone-type db))]
    (loop [result []
           lhops hops
           starttime time]
      (if (empty? lhops)
        result
        (let [hop (first lhops)
              distance (util/distance
                         (util/position (one :buildings (:from hop) db))
                         (util/position (one :buildings (:to hop) db)))
              endtime (long (+ starttime
                               (* 1000
                                  (util/travel-time
                                    (util/position (one :buildings (:from hop) db))
                                    (util/position (one :buildings (:to hop) db))
                                    speed))))]
          (recur (conj result {:hop/route     routeid
                               :hop/start     (:from hop)
                               :hop/end       (:to hop)
                               :hop/starttime starttime
                               :hop/endtime   endtime
                               :hop/distance  distance})
                 (drop 1 lhops)
                 (+ endtime 3000)))))))

(defn connections-with-shop-cust [hiveids shopid custid db]
  (let [db-after-shop (if (nil? shopid)
                        db
                        (:db-after (d/with db (gen-connections db shopid))))
        db-after-cust (if (nil? custid)
                        db-after-shop
                        (:db-after (d/with db-after-shop (gen-connections db-after-shop custid shopid))))]
    (concat (saved-connections hiveids db) (saved-connections [shopid] db-after-cust) (saved-connections [custid] db-after-cust))))

(defn charge-at-time [droneid time db]
  (let [db-as-of-time (d/as-of db (java.util.Date. time))
        hops (sort-by :hop/endtime (hops-for-drone droneid db-as-of-time))]
    (if (empty? hops)
      100
      (let [latest-hop (last hops)
            drone (one :drones droneid db-as-of-time)
            dronetype (one :dronetypes (:db/id (:drone/type drone)) db)
            chargetime (:dronetype/chargetime dronetype)
            charge-after-hop (:hop/endcharge latest-hop)
            seconds-since-hop (/ 1000 (- time (:hop/endtime latest-hop)))
            charged-since-hop (/ seconds-since-hop chargetime)
            charge-now (+ charge-after-hop charged-since-hop)]
        (if (> 100 charge-now)
          100
          charge-now)))))

(defn assoc-charge [drones time db]
  (map
    #(assoc % :charge (charge-at-time (:db/id %) time db))
    drones))

(defn dissoc-needed [outgoing-after-now drones db]
  (loop [hops outgoing-after-now
         drones drones]
    (if (empty? hops)
      drones
      (if (empty? drones)
        nil
        (let [hop (first hops)
              satisfying-drones (filter #(util/reachable-with-charge (:hop/distance hop)
                                                                     5000
                                                                     (charge-at-time (:db/id %)
                                                                                     (:hop/starttime hop)
                                                                                     db))
                                        drones)
              lowest-drone (last (sort-by :charge (map #(assoc %
                                                          :charge
                                                          (charge-at-time (:db/id %)
                                                                          (:hop/starttime hop)
                                                                          db))
                                                       satisfying-drones)))]
          (recur (drop 1 hops)
                 (remove #(= (:db/id %) (:db/id lowest-drone)) drones)))))))


(defn leftover-drones [hiveid time db]
  (let [drones (drones-for-hive hiveid db)
        outgoing-after-now (outgoing-hops-after hiveid (.getTime (java.util.Date.)) db)
        incoming-until-time (incoming-hops-until hiveid time db)
        drones-of-incoming (mapv #(one :drones (:hop/drone %) db) incoming-until-time)
        drones (concat drones drones-of-incoming)]
    (dissoc-needed outgoing-after-now drones db)))


(defn hivecosts [hiveids time db]
  (mapv
    (fn [hive]
      (let [hiveid (:db/id hive)]
        (if (nil? (leftover-drones hiveid time db))
          (identity {:db/id     hiveid
                     :hive/cost 21})
          (let [demand (:hive/demand (:building/hive hive))
                drones (drones-for-hive hiveid db)
                numdrones (count drones)
                outgoing-now (outgoing-hops-after hiveid (.getTime (java.util.Date.)) db)
                incoming-until-time (incoming-hops-until hiveid time db)
                drones-at-time (+ (- numdrones (count outgoing-now)) (count incoming-until-time))
                free-at-time (- drones-at-time demand)
                percent-takeout (if (= 0 drones-at-time)
                                  101
                                  (* 100 (/ 1 drones-at-time)))
                cost (if (< free-at-time 1)
                       (* 3 percent-takeout)
                       percent-takeout)
                mapped-cost (if (= demand -1)
                              1
                              (util/map-num cost 0 300 1 20))]
            {:db/id     hiveid
             :hive/cost mapped-cost}))))
    (all :hives hiveids db)))

(defn outgoing-timeframe [starttime endtime hiveid db]
  (or (d/q '[:find (count ?hop) .
             :in $ ?starttime ?endtime ?hiveid
             :where [?hop :hop/start ?hiveid] [?hop :hop/starttime ?hoptime] (or-join [?hoptime ?starttime ?endtime]
                                                                                      (and [(< ?starttime ?hoptime)] [(> ?endtime ?hoptime)]))]
           db
           starttime
           endtime
           hiveid) 0))

(defn ongoing-routes [time db]
  (d/q '[:find [(pull ?route subquery) ...]
         :in $ ?time subquery
         :where [?hop :hop/route ?route] [?hop :hop/starttime ?starttime] [?hop :hop/endtime ?endtime] (or-join [?time ?starttime ?endtime]
                                                                                                                (and [(< ?time ?endtime)]
                                                                                                                     [(> ?time ?starttime)]))]
       db
       time
       (get rules/fields :routes)))

(defn hive-statistics-timeframe [hiveid starttime endtime db]
  (let [drones-at-time (count (drones-for-hive hiveid (d/as-of db (java.util.Date. starttime))))
        incoming (incoming-hops-timeframe hiveid starttime endtime db)
        outgoing (outgoing-hops-timeframe hiveid starttime endtime db)
        incoming-annotated (map #(identity {:mod 1 :time (:hop/endtime %)}) incoming)
        outgoing-annotated (map #(identity {:mod -1 :time (:hop/starttime %)}) outgoing)
        hops (concat incoming-annotated outgoing-annotated)
        sorted-hops (sort-by :time hops)]
    (sort-by :time
             (loop [values [{:time  starttime
                             :value drones-at-time}]
                    lhops sorted-hops
                    lastval drones-at-time]
               (if (empty? lhops)
                 (conj values {:time  endtime
                               :value lastval})
                 (let [lhop (first lhops)
                       newval (+ lastval (:mod lhop))]
                   (recur (conj values {:time  (:time lhop)
                                        :value newval})
                          (rest lhops)
                          newval)))))))

(defn drone-end-hop [droneid db]
  (let [hops (hop-ids droneid db)
        hops-with-times (all :hops hops db)
        sorted (sort-by :hop/endtime hops-with-times)
        last-hop (last sorted)]
    (println hops)
    (println hops-with-times)
    (println sorted)
    last-hop))


(defn drone-ids-at-time [buildingid time db]
  (let [droneids (all-ids :drones db)
        drones-at-hive (filter #(if (nil? (drone-end-hop % db))
                                  (= buildingid (:drone/hive (one :drones % db)))
                                  (and (> time (:hop/endtime (drone-end-hop % db)))
                                       (= (:hop/end (drone-end-hop % db)) buildingid)))
                               droneids)]
    drones-at-hive))

(defn time-capable [droneid starttime distance db]
  (let [dronetype (:drone/type (one :drones droneid db))
        req-charge (util/used-charge dronetype distance)
        charge (charge-at-time droneid starttime db)
        diff (- req-charge charge)]
    (if (> 0 diff)
      starttime
      (* (:dronetype/chargetime dronetype) (/ diff 1000)))))



(defn find-drone-and-time [buildingid time distance db]
  (let [droneids (drone-ids-at-time buildingid time db)]
    (println droneids)
    (loop [ids droneids
           earliest {:starttime 100000000000000
                     :droneid   0}]
      (if (empty? ids)
        earliest
        (if (> earliest (time-capable (first ids) time distance db))
          (recur (drop 1 ids)
                 {:starttime (time-capable (first ids) time distance db)
                  :droneid   (first ids)})
          (recur (drop 1 ids)
                 earliest))))))



