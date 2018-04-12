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
         result []]
    (if (empty? hops)
      result
      (let [{from :from
             to   :to} hop
            from-hive (:building/hive (queries/one :buildings from db))
            from-hive (if (nil? from-hive)
                        (:building/shop (queries/one :buildings from db))
                        from-hive)
            from-hive (if (nil? from-hive)
                        (:building/customer (queries/one :buildings from db))
                        from-hive)
            distance (queries/distance from to db)
            {starttime :starttime
             droneid   :droneid} (queries/find-drone-and-time from-hive starttime distance db)
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
               (conj result gen-hop))))))

