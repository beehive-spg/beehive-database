(ns beehive-database.datomic.actions.queries
  (require [datomic.api :as d]
           [beehive-database.datomic.init.schema :as s]
           [beehive-database.util :as u]
           [beehive-database.datomic.actions.rules :as r]
           [beehive-database.datomic.actions.data :refer :all]))

(defn all-hives
  ([]
   (d/q '[:find (pull ?e subquery)
          :in $ subquery
          :where
          [?e :building/hive _]] @db r/hive-fields))
  ([& ids]
   (d/q '[:find (pull ?ids subquery)
          :in $ subquery [?ids ...]
          :where
          [?ids]] @db r/hive-fields ids)))

(defn all-shops
  ([]
   (d/q '[:find (pull ?e subquery)
          :in $ subquery
          :where
          [?e :building/shop _]] @db r/shop-fields))
  ([& ids]
   (d/q '[:find (pull ?ids subquery)
          :in $ subquery [?ids ...]
          :where
          [?ids]] @db r/shop-fields ids)))

(defn all-customers
  ([]
   (d/q '[:find (pull ?e subquery)
          :in $ subquery
          :where
          [?e :building/customer _]] @db r/customer-fields))
  ([& ids]
   (d/q '[:find (pull ?ids subquery)
          :in $ subquery [?ids ...]
          :where
          [?ids]] @db r/customer-fields ids)))

(defn all-drones
  ([]
   (d/q '[:find (pull ?e subquery)
          :in $ subquery
          :where
          [?e :drone/name _]] @db r/drone-fields))
  ([& ids]
   (d/q '[:find (pull ?ids subquery)
          :in $ subquery [?ids ...]
          :where
          [?ids]] @db r/drone-fields ids)))

(defn all-predictions
  ([]
   (d/q '[:find (pull ?e subquery)
          :in $ subquery
          :where
          [?e :prediction/value _]] @db r/prediction-fields))
  ([& ids]
   (d/q '[:find (pull ?ids subquery)
          :in $ subquery [?ids ...]
          :where
          [?ids]] @db r/prediction-fields ids)))

(defn all-hops
  ([]
   (d/q '[:find (pull ?e subquery)
          :in $ subquery
          :where
          [?e :hop/drone _]] @db r/hop-fields))
  ([& ids]
   (d/q '[:find (pull ?ids subquery)
          :in $ subquery [?ids ...]
          :where
          [?ids]] @db r/hop-fields ids)))

(defn all-routes
  ([]
   (d/q '[:find (pull ?e subquery)
          :in $ subquery
          :where
          [?e :route/hops _]] @db r/route-fields))
  ([& ids]
   (d/q '[:find (pull ?ids subquery)
          :in $ subquery [?ids ...]
          :where
          [?ids]] @db r/route-fields ids)))

(defn all-orders
  ([]
   (d/q '[:find (pull ?e subquery)
          :in $ subquery
          :where
          [?e :order/customer _]] @db r/order-fields))
  ([& ids]
   (d/q '[:find (pull ?ids subquery)
          :in $ subquery [?ids ...]
          :where
          [?ids]] @db r/order-fields ids)))

(defn get-max-range []
  (first (first (d/q '[:find (max ?e)
                       :where
                       [_ :drone/range ?e]] @db))))

(defn get-reachable [x y]
  (let [hives (all-hives)
        max-range (get-max-range)]
    (map
      #(:db/id (first %))
      (filter
        #(< (u/distance [x y]
                        [(:building/xcoord (first %)) (:building/ycoord (first %))])
            (/ max-range 1000))
        hives))))

(defn is-reachable [id1 id2]
  (let [[hive1 hive2] (all-hives id1 id2)]
    (u/reachable
      [(:building/xcoord (first hive1)) (:building/ycoord (first hive2))]
      [(:building/xcoord (first hive2)) (:building/ycoord (first hive2))]
      (get-max-range))))
