(ns beehive-database.datomic.actions.queries
  (require [datomic.api :as d]
           [beehive-database.datomic.init.schema :as s]
           [beehive-database.util :as u]
           [beehive-database.datomic.actions.rules :as r]
           [beehive-database.datomic.actions.data :refer :all]))

(defn all-hives
  ([]
   (d/q '[:find (pull ?e r/hive-fields)
          :where
          [?e :building/hive _]] @db))
  ([& ids]
   (d/q '[:find (pull ?ids r/hive-fields)
          :in $ [?ids ...]
          :where
          [?ids]] @db ids)))

(defn all-shops
  ([]
   (d/q '[:find (pull ?e r/shop-fields)
          :where
          [?e :building/shop _]] @db))
  ([& ids]
   (d/q '[:find (pull ?ids r/shop-fields)
          :in $ [?ids ...]
          :where
          [?ids]] @db ids)))

(defn all-customers
  ([]
   (d/q '[:find (pull ?e r/customer-fields)
          :where
          [?e :building/customer _]] @db))
  ([& ids]
   (d/q '[:find (pull ?ids r/customer-fields)
          :in $ [?ids ...]
          :where
          [?ids]] @db ids)))

(defn all-drones
  ([]
   (d/q '[:find (pull ?e r/drone-fields)
          :where
          [?e :drone/name _]] @db))
  ([& ids]
   (d/q '[:find (pull ?ids r/drone-fields)
          :in $ [?ids ...]
          :where
          [?ids]] @db ids)))

(defn all-predictions
  ([]
   (d/q '[:find (pull ?e r/prediction-fields)
          :where
          [?e :prediction/value _]] @db))
  ([& ids]
   (d/q '[:find (pull ?ids r/prediction-fields)
          :in $ [?ids ...]
          :where
          [?ids]] @db ids)))

(defn all-hops
  ([]
   (d/q '[:find (pull ?e r/hop-fields)
          :where
          [?e :hop/drone _]] @db))
  ([& ids]
   (d/q '[:find (pull ?ids r/hop-fields)
          :in $ [?ids ...]
          :where
          [?ids]] @db ids)))

(defn all-routes
  ([]
   (d/q '[:find (pull ?e r/route-fields)
          :where
          [?e :route/hops _]] @db))
  ([& ids]
   (d/q '[:find (pull ?ids r/route-fields)
          :in $ [?ids ...]
          :where
          [?ids]] @db ids)))

(defn all-orders
  ([
    (d/q '[:find (pull ?e r/order-fields)
           :where
           [?e :order/customer _]] @db)])
  ([& ids]
   (d/q '[:find (pull ?ids r/order-fields)
          :in $ [?ids ...]
          :where
          [?ids]] @db ids)))

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
