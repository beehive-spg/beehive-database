(ns beehive-database.datomic.actions.data
  (:require [datomic.api :as datomic]))

(def uri "datomic:mem://beehive")

(datomic/create-database uri)

(def conn
  (datomic/connect uri))

(defn db []
  (datomic/db conn))