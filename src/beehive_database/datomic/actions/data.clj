(ns beehive-database.datomic.actions.data
  (:require [datomic.api :as d]))

(def uri "datomic:mem://beehive")

(d/create-database uri)

(def conn
  (d/connect uri))

(defn db []
  (d/db conn))