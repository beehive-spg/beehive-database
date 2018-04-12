(ns beehive-database.datomic.actions.tx-generators
  (:require [beehive-database.util :as util]
            [beehive-database.datomic.actions.queries :as queries]))

(defn gen-connections [db buildingid & extra-buildingids]
  (mapv
    (fn [mapped-id]
      {:connection/start    buildingid
       :connection/end      mapped-id
       :connection/distance (util/distance
                              (queries/building-position buildingid db)
                              (queries/building-position mapped-id db))})
    (queries/reachable-ids buildingid db extra-buildingids)))

(defn gen-hops [hops routeid time db]
  (loop [hops hops
         hop (first hops)
         starttime time
         last-droneid 0
         result []]
    (println hops)
    (if (empty? hops)
      result
      (let [{from :from
             to   :to} hop
            from-hive (:building/hive (queries/one :buildings from db))
            distance (queries/distance from to db)
            (println from-hive)
            (println distance)
            (println from)
            {starttime :starttime
             droneid   :droneid} (if (nil? from-hive)
                                   {:starttime starttime
                                    :droneid   last-droneid}
                                   (queries/find-drone-and-time (:db/id (first from-hive)) starttime distance db))
            traveltime (queries/travel-time from to droneid db)
            endtime (+ starttime traveltime)
            new-hops (drop 1 hops)
            gen-hop {:hop/route     routeid
                     :hop/start     from
                     :hop/end       hop
                     :hop/starttime starttime
                     :hop/endtime   endtime
                     :hop/distance  distance
                     :hop/drone     droneid}]
        (recur new-hops
               (first new-hops)
               endtime
               droneid
               (conj result gen-hop))))))

