(ns beehive-database.datomic.init.schema
  (:require [datomic.api :as d]
            [beehive-database.util :as util]
            [datomic-schema.schema :as datomic-schema]))

(def parts [(datomic-schema/part "user")])

(def dbschema
  [(datomic-schema/schema building
                          (datomic-schema/fields
                           [address :string]
                           [xcoord :float]
                           [ycoord :float]
                           [hive :ref :component]
                           [shop :ref :component :many]
                           [customer :ref :component :many]))
   (datomic-schema/schema hive
                          (datomic-schema/fields
                           [name :string]
                           [demand :long]))
   (datomic-schema/schema shop
                          (datomic-schema/fields
                           [name :string]))
   (datomic-schema/schema customer
                          (datomic-schema/fields
                           [name :string]))
   (datomic-schema/schema drone
                          (datomic-schema/fields
                           [name :string]
                           [status :enum [:idle :flying :charging]]
                           [type :ref]
                           [hive :ref]))
   (datomic-schema/schema prediction
                          (datomic-schema/fields
                           [value :float]
                           [hive :ref]))
   (datomic-schema/schema hop
                          (datomic-schema/fields
                           [drone :ref]
                           [start :ref]
                           [end :ref]
                           [starttime :long]
                           [endtime :long]
                           [distance :float]
                           [route :ref]))
   (datomic-schema/schema route
                          (datomic-schema/fields
                           [origin :enum [:order :distribution]]))
   (datomic-schema/schema order
                          (datomic-schema/fields
                           [shop :ref]
                           [customer :ref]
                           [route :ref]
                           [source :enum [:gui :generated]]))
   (datomic-schema/schema connection
                          (datomic-schema/fields
                           [start :ref]
                           [end :ref]
                           [distance :float]))
   (datomic-schema/schema dronetype
                          (datomic-schema/fields
                           [range :long]
                           [name :string]
                           [speed :long]
                           [chargetime :long]
                           [default :boolean]))
   (datomic-schema/dbfn connections [db hive] :db.part/user
                        (mapv
                         (fn [x]
                           {:db/id               (datomic.api/tempid :db.part/user)
                            :connection/start    hive
                            :connection/end      (:db/id x)
                            :connection/distance (beehive-database.util/distance
                                                   (beehive-database.util/position
                                                     (beehive-database.datomic.actions.queries/one :hives hive db))
                                                   (beehive-database.util/position x))})
                         (beehive-database.datomic.actions.queries/reachable hive db)))
   (datomic-schema/dbfn mkroute [db hops routeid time] :db.part/user
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