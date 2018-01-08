(ns beehive-database.datomic.actions.data
  (:require [datomic.api :as d]))

(def uri "datomic:mem://beehive")

(d/create-database uri)

(def conn
  (d/connect uri))

(def db
  (atom (d/db conn)))

(defn refresh []
  (reset! db (d/db conn)))