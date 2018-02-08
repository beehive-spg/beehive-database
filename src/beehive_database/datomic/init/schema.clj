(ns beehive-database.datomic.init.schema
  (:require [datomic.api :as d]
            [beehive-database.util :as u]
            [datomic-schema.schema :as schema]))

(def parts [(schema/part "user")])

(def dbschema
  [(schema/schema building
                  (schema/fields
                    [address :string]
                    [xcoord :float]
                    [ycoord :float]
                    [hive :ref :component]
                    [shop :ref :component :many]
                    [customer :ref :component :many]))
   (schema/schema hive
                  (schema/fields
                    [name :string]
                    [demand :long]))
   (schema/schema shop
                  (schema/fields
                    [name :string]))
   (schema/schema customer
                  (schema/fields
                    [name :string]))
   (schema/schema drone
                  (schema/fields
                    [name :string]
                    [status :enum ["IDLE" "FLYING" "CHARGING"]]
                    [type :ref]
                    [hive :ref]))
   (schema/schema prediction
                  (schema/fields
                    [value :float]
                    [hive :ref]))
   (schema/schema hop
                  (schema/fields
                    [drone :ref]
                    [start :ref]
                    [end :ref]
                    [starttime :long]
                    [endtime :long]
                    [distance :float]
                    [route :ref]))
   (schema/schema route
                  (schema/fields
                    [origin :enum ["ORDER" "DISTRIBUTION"]]))
   (schema/schema order
                  (schema/fields
                    [shop :ref]
                    [customer :ref]
                    [route :ref]
                    [source :enum ["GUI" "GENERATED"]]))
   (schema/schema connection
                  (schema/fields
                    [start :ref]
                    [end :ref]
                    [distance :float]))
   (schema/schema dronetype
                  (schema/fields
                    [range :long]
                    [name :string]
                    [speed :long]
                    [chargetime :long]
                    [default :boolean]))
   (schema/dbfn connections [db hive] :db.part/user
                (mapv
                  (fn [x]
                    {:db/id               (d/tempid :db.part/user)
                     :connection/start    hive
                     :connection/end      (:db/id x)
                     :connection/distance (beehive-database.util/distance
                                            (beehive-database.util/position
                                              (beehive-database.datomic.actions.queries/one :hives hive db))
                                            (beehive-database.util/position x))})
                  (beehive-database.datomic.actions.queries/reachable hive db)))
   (schema/dbfn mkroute [db hops routeid time] :db.part/user
                (let [speed (:dronetype/speed (beehive-database.datomic.actions.queries/default-drone-type db))]
                  (loop [result []
                         lhops hops
                         starttime time]
                    (let [endtime (long (+
                                          starttime
                                          (* 1000 (beehive-database.util/travel-time
                                                    (beehive-database.util/position (beehive-database.datomic.actions.queries/one :hives (:from (first hops)) db))
                                                    (beehive-database.util/position (beehive-database.datomic.actions.queries/one :hives (:to (first hops)) db))
                                                    speed))))]
                      (if (empty? lhops)
                        result
                        (recur (conj result {:hop/route     routeid
                                             :hop/start     (:from (first lhops))
                                             :hop/end       (:to (first lhops))
                                             :hop/starttime starttime
                                             :hop/endtime   endtime})
                               (drop 1 lhops)
                               (+ endtime 3000)))))))])