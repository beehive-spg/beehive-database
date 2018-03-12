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
                            [demand :float]))
   (datomic-schema/schema shop
                          (datomic-schema/fields
                            [name :string]))
   (datomic-schema/schema customer
                          (datomic-schema/fields
                            [name :string]))
   (datomic-schema/schema drone
                          (datomic-schema/fields
                            [name :string]
                            [status :enum [:idle :flying]]
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
                            [route :ref]
                            [endcharge :float]))
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
                            [default :boolean]))])

