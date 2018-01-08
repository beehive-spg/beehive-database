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
            :db/ident              :hive/drones
            :db/valueType          :db.type/ref
            :db/cardinality        :db.cardinality/many
            :db/doc                "The drones of a hive"
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
             :db/doc         "Status of the drone"}

            {:db/ident :status/IDLE}
            {:db/ident :status/FLYING}
            {:db/ident :status/CHARGING}])

(def tables [building hive shop customer drone])