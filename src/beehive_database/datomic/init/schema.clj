(ns beehive-database.datomic.init.schema
  (require [datomic.api :as d]))

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
            :db/ident              :hive/reachable
            :db/valueType          :db.type/ref
            :db/cardinality        :db.cardinality/many
            :db/doc                "The reachable hives of a hive"
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

            {:db/id                 (d/tempid :db.part/db)
             :db/ident              :drone/range
             :db/valueType          :db.type/long
             :db/cardinality        :db.cardinality/one
             :db/doc                "The range of a drone"
             :db.install/_attribute :db.part/db}

            {:db/id          (d/tempid :db.part/db)
             :db/ident       :drone/status
             :db/valueType   :db.type/ref
             :db/cardinality :db.cardinality/one
             :db/doc         "Status of the drone"}

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
           :db.install/_attribute :db.part/db}])

(def route [{:db/id                 (d/tempid :db.part/db)
             :db/ident              :route/hops
             :db/valueType          :db.type/ref
             :db/isComponent        true
             :db/cardinality        :db.cardinality/many
             :db/doc                "The hops of a route"
             :db.install/_attribute :db.part/db}

            {:db/id                 (d/tempid :db.part/db)
             :db/ident              :route/origin
             :db/valueType          :db.type/ref
             :db/isComponent        true
             :db/cardinality        :db.cardinality/many
             :db/doc                "The status of a route"
             :db.install/_attribute :db.part/db}

            {:db/ident :origin/GUI}
            {:db/ident :origin/GENERATED}])


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
             :db.install/_attribute :db.part/db}])

(def tables [building hive shop customer drone prediction hop route order])