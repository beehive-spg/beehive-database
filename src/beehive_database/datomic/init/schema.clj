(ns beehive-database.datomic.init.schema
  (:require [datomic.api :as d]
            [beehive-database.util :as u]))

(def building [{:db/id                 (d/tempid :db.part/db)
                :db/ident              :building/address
                :db/valueType          :db.type/string
                :db/cardinality        :db.cardinality/one
                :db/doc                "The Address of a Building"
                :db.install/_attribute :db.part/db}

               {:db/id                 (d/tempid :db.part/db)
                :db/ident              :building/xcoord
                :db/valueType          :db.type/float
                :db/cardinality        :db.cardinality/one
                :db/doc                "The X Coordinate of a Building"
                :db.install/_attribute :db.part/db}

               {:db/id                 (d/tempid :db.part/db)
                :db/ident              :building/ycoord
                :db/valueType          :db.type/float
                :db/cardinality        :db.cardinality/one
                :db/doc                "The Y Coordinate of a Building"
                :db.install/_attribute :db.part/db}

               {:db/id                 (d/tempid :db.part/db)
                :db/ident              :building/hive
                :db/isComponent        true
                :db/valueType          :db.type/ref
                :db/cardinality        :db.cardinality/one
                :db/doc                "The hive of a building"
                :db.install/_attribute :db.part/db}

               {:db/id                 (d/tempid :db.part/db)
                :db/ident              :building/shop
                :db/isComponent        true
                :db/valueType          :db.type/ref
                :db/cardinality        :db.cardinality/many
                :db/doc                "The shops of a building"
                :db.install/_attribute :db.part/db}

               {:db/id                 (d/tempid :db.part/db)
                :db/ident              :building/customer
                :db/isComponent        true
                :db/valueType          :db.type/ref
                :db/cardinality        :db.cardinality/many
                :db/doc                "The customers of a building"
                :db.install/_attribute :db.part/db}])

(def hive [{:db/id                 (d/tempid :db.part/db)
            :db/ident              :hive/name
            :db/unique             :db.unique/identity
            :db/valueType          :db.type/string
            :db/cardinality        :db.cardinality/one
            :db/doc                "The name of a hive"
            :db.install/_attribute :db.part/db}

           {:db/id                 (d/tempid :db.part/db)
            :db/ident              :hive/demand
            :db/valueType          :db.type/long
            :db/cardinality        :db.cardinality/one
            :db/doc                "The demand of a hive"
            :db.install/_attribute :db.part/db}])


(def shop [{:db/id                 (d/tempid :db.part/db)
            :db/ident              :shop/name
            :db/valueType          :db.type/string
            :db/cardinality        :db.cardinality/one
            :db/doc                "The name of a shop"
            :db.install/_attribute :db.part/db}])

(def customer [{:db/id                 (d/tempid :db.part/db)
                :db/ident              :customer/name
                :db/valueType          :db.type/string
                :db/cardinality        :db.cardinality/one
                :db/doc                "The name of a customer"
                :db.install/_attribute :db.part/db}])

(def drone [{:db/id                 (d/tempid :db.part/db)
             :db/ident              :drone/name
             :db/valueType          :db.type/string
             :db/cardinality        :db.cardinality/one
             :db/doc                "The name of a drone"
             :db.install/_attribute :db.part/db}

            {:db/id          (d/tempid :db.part/db)
             :db/ident       :drone/status
             :db/valueType   :db.type/ref
             :db/cardinality :db.cardinality/one
             :db/doc         "The status of a drone"}

            {:db/id          (d/tempid :db.part/db)
             :db/ident       :drone/type
             :db/valueType   :db.type/ref
             :db/cardinality :db.cardinality/one
             :db/doc         "The type of a drone"}

            {:db/id                 (d/tempid :db.part/db)
             :db/ident              :drone/hive
             :db/valueType          :db.type/ref
             :db/cardinality        :db.cardinality/one
             :db/doc                "The hive of a drone"
             :db.install/_attribute :db.part/db}

            {:db/ident :status/IDLE}
            {:db/ident :status/FLYING}
            {:db/ident :status/CHARGING}])

(def prediction [{:db/id                 (d/tempid :db.part/db)
                  :db/ident              :prediction/value
                  :db/valueType          :db.type/float
                  :db/cardinality        :db.cardinality/one
                  :db/doc                "The value of a prediction"
                  :db.install/_attribute :db.part/db}

                 {:db/id                 (d/tempid :db.part/db)
                  :db/ident              :prediction/hive
                  :db/valueType          :db.type/ref
                  :db/cardinality        :db.cardinality/one
                  :db/doc                "The hive of a prediction"
                  :db.install/_attribute :db.part/db}])

(def hop [{:db/id                 (d/tempid :db.part/db)
           :db/ident              :hop/drone
           :db/valueType          :db.type/ref
           :db/cardinality        :db.cardinality/one
           :db/doc                "The drone of a hop"
           :db.install/_attribute :db.part/db}

          {:db/id                 (d/tempid :db.part/db)
           :db/ident              :hop/start
           :db/valueType          :db.type/ref
           :db/cardinality        :db.cardinality/one
           :db/doc                "The start of a hop"
           :db.install/_attribute :db.part/db}

          {:db/id                 (d/tempid :db.part/db)
           :db/ident              :hop/end
           :db/valueType          :db.type/ref
           :db/cardinality        :db.cardinality/one
           :db/doc                "The end of a hop"
           :db.install/_attribute :db.part/db}

          {:db/id          (d/tempid :db.part/db)
           :db/ident       :hop/starttime
           :db/valueType   :db.type/long
           :db/cardinality :db.cardinality/one
           :db/doc         "The starting time of a hop"}

          {:db/id          (d/tempid :db.part/db)
           :db/ident       :hop/endtime
           :db/valueType   :db.type/long
           :db/cardinality :db.cardinality/one
           :db/doc         "The ending time of a hop"}

          {:db/id          (d/tempid :db.part/db)
           :db/ident       :hop/distance
           :db/valueType   :db.type/float
           :db/cardinality :db.cardinality/one
           :db/doc         "The distance of a hop"}

          {:db/id          (d/tempid :db.part/db)
           :db/ident       :hop/route
           :db/valueType   :db.type/ref
           :db/cardinality :db.cardinality/one
           :db/doc         "The route of a hop"}])

(def route [{:db/id                 (d/tempid :db.part/db)
             :db/ident              :route/origin
             :db/valueType          :db.type/ref
             :db/cardinality        :db.cardinality/one
             :db/doc                "The origin of a route"
             :db.install/_attribute :db.part/db}

            {:db/ident :origin/ORDER}
            {:db/ident :origin/DISTRIBUTION}])

(def order [{:db/id                 (d/tempid :db.part/db)
             :db/ident              :order/shop
             :db/valueType          :db.type/ref
             :db/cardinality        :db.cardinality/one
             :db/doc                "The shop of an order"
             :db.install/_attribute :db.part/db}

            {:db/id                 (d/tempid :db.part/db)
             :db/ident              :order/customer
             :db/valueType          :db.type/ref
             :db/cardinality        :db.cardinality/one
             :db/doc                "The customer of an order"
             :db.install/_attribute :db.part/db}

            {:db/id                 (d/tempid :db.part/db)
             :db/ident              :order/route
             :db/valueType          :db.type/ref
             :db/cardinality        :db.cardinality/one
             :db/doc                "The route of an order"
             :db.install/_attribute :db.part/db}

            {:db/id          (d/tempid :db.part/db)
             :db/ident       :order/source
             :db/valueType   :db.type/ref
             :db/cardinality :db.cardinality/one
             :db/doc         "The source of an order"}

            {:db/ident :source/GUI}
            {:db/ident :source/GENERATED}])

(def connection [{:db/id          (d/tempid :db.part/db)
                  :db/ident       :connection/start
                  :db/valueType   :db.type/ref
                  :db/cardinality :db.cardinality/one
                  :db/doc         "The start of a connection"}

                 {:db/id          (d/tempid :db.part/db)
                  :db/ident       :connection/end
                  :db/valueType   :db.type/ref
                  :db/cardinality :db.cardinality/one
                  :db/doc         "The end of a connection"}

                 {:db/id          (d/tempid :db.part/db)
                  :db/ident       :connection/distance
                  :db/valueType   :db.type/float
                  :db/cardinality :db.cardinality/one
                  :db/doc         "The distance of a connection"}])

(def drone-types [{:db/id                 (d/tempid :db.part/db)
                   :db/ident              :dronetype/range
                   :db/valueType          :db.type/long
                   :db/cardinality        :db.cardinality/one
                   :db/doc                "The range of a drone type"
                   :db.install/_attribute :db.part/db}

                  {:db/id                 (d/tempid :db.part/db)
                   :db/ident              :dronetype/name
                   :db/valueType          :db.type/string
                   :db/cardinality        :db.cardinality/one
                   :db/doc                "The name of a drone type"
                   :db.install/_attribute :db.part/db}

                  {:db/id                 (d/tempid :db.part/db)
                   :db/ident              :dronetype/speed
                   :db/valueType          :db.type/long
                   :db/cardinality        :db.cardinality/one
                   :db/doc                "The speed of a drone type"
                   :db.install/_attribute :db.part/db}

                  {:db/id                 (d/tempid :db.part/db)
                   :db/ident              :dronetype/chargetime
                   :db/valueType          :db.type/long
                   :db/cardinality        :db.cardinality/one
                   :db/doc                "The charging time of a drone type"
                   :db.install/_attribute :db.part/db}

                  {:db/id          (d/tempid :db.part/db)
                   :db/ident       :dronetype/default
                   :db/valueType   :db.type/boolean
                   :db/cardinality :db.cardinality/one
                   :db/doc         "Whether or not the drone type is the default type"}])

(def fns [{:db/id    (d/tempid :db.part/db)
           :db/ident :connections
           :db/fn    #db/fn {:lang   "clojure"
                             :params [db hive]
                             :code   (mapv
                                       (fn [x]
                                         {:db/id               (d/tempid :db.part/user)
                                          :connection/start    hive
                                          :connection/end      (:db/id x)
                                          :connection/distance (beehive-database.util/distance
                                                                 (beehive-database.util/position
                                                                   (beehive-database.datomic.actions.queries/one :hives hive db))
                                                                 (beehive-database.util/position x))})
                                       (beehive-database.datomic.actions.queries/reachable hive db))}}

          {:db/id    (d/tempid :db.part/db)
           :db/ident :mkroute
           :db/fn    #db/fn {:lang   "clojure"
                             :params [db hops routeid time]
                             :code   (let [speed (:dronetype/speed (beehive-database.datomic.actions.queries/default-drone-type db))]
                                       (loop [result []
                                              lhops hops
                                              starttime time]
                                         (let [endtime (long (+
                                                               (* 1000 starttime)
                                                               (beehive-database.util/travel-time
                                                                 (beehive-database.util/position (beehive-database.datomic.actions.queries/one :hives (:from (first hops)) db))
                                                                 (beehive-database.util/position (beehive-database.datomic.actions.queries/one :hives (:to (first hops)) db))
                                                                 speed)))]
                                           (if (empty? lhops)
                                             result
                                             (recur (conj result {:hop/route     routeid
                                                                  :hop/start     (:from (first lhops))
                                                                  :hop/end       (:to (first lhops))
                                                                  :hop/starttime starttime
                                                                  :hop/endtime   endtime})
                                                    (drop 1 lhops)
                                                    (+ endtime 3000))))))}}])


(defn dbfun [db hops routeid time]
  (let [speed (:dronetype/speed (beehive-database.datomic.actions.queries/default-drone-type db))]
    (loop [result []
           lhops hops
           starttime time]
      (let [endtime (long (+
                            starttime
                            (beehive-database.util/travel-time
                              (beehive-database.util/position (beehive-database.datomic.actions.queries/one :hives (:from (first hops)) db))
                              (beehive-database.util/position (beehive-database.datomic.actions.queries/one :hives (:to (first hops)) db))
                              speed)))]
        (if (empty? lhops)
          result
          (recur (conj result {:hop/route     routeid
                               :hop/start     (:from (first lhops))
                               :hop/end       (:to (first lhops))
                               :hop/starttime starttime
                               :hop/endtime   endtime})
                 (drop 1 lhops)
                 (+ endtime 3000)))))))





(def tables [building hive shop customer drone prediction hop route order connection drone-types fns])