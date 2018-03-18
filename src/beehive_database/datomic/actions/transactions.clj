(ns beehive-database.datomic.actions.transactions
  (:require [datomic.api :as d]
            [beehive-database.datomic.actions.data :refer :all]
            [beehive-database.datomic.actions.queries :as queries]
            [beehive-database.util :as util]))

(defn transact->entity [conn data table]
  (let [sa (println data)
        id (d/tempid :db.part/user)
        tx @(d/transact conn [(assoc (first data) :db/id id)])
        db-after (:db-after tx)
        tempids (:tempids tx)
        real-id (d/resolve-tempid db-after tempids id)]
    {:entity (queries/one table real-id db-after)
     :tx     tx}))


(defn add-building [{:keys [address xcoord ycoord]}]
  (let [result (transact->entity conn
                                 [{:building/address address
                                   :building/xcoord  xcoord
                                   :building/ycoord  ycoord}]
                                 :buildings)]
    result))

(defn add-hive [{:keys [address xcoord ycoord name]}]
  (let [result (transact->entity conn
                                 [{:building/address address
                                   :building/xcoord  xcoord
                                   :building/ycoord  ycoord
                                   :building/hive    {:hive/name   name
                                                      :hive/demand -1.0}}]
                                 :hives)
        {entity :entity
         tx     :tx} result
        id (:db/id entity)
        tx @(d/transact conn (queries/gen-connections (:db-after tx) id))
        result (assoc result :tx tx)]
    result))


(defn add-shop [{:keys [address xcoord ycoord name]}]
  (let [result (transact->entity conn
                                 [{:building/address address
                                   :building/xcoord  xcoord
                                   :building/ycoord  ycoord
                                   :building/shop    {:shop/name name}}]
                                 :shops)]
    result))


(defn add-customer [{:keys [address xcoord ycoord name]}]
  (let [result (transact->entity conn
                                 [{:building/address  address
                                   :building/xcoord   xcoord
                                   :building/ycoord   ycoord
                                   :building/customer {:customer/name name}}]
                                 :customers)]
    result))

(defn add-drone [{:keys [hiveid name type status]}]
  (let [result (transact->entity conn
                                 [{:drone/name   name
                                   :drone/type   (if (nil? type)
                                                   (:db/id (queries/default-drone-type (d/db conn)))
                                                   type)
                                   :drone/status status
                                   :drone/hive   hiveid}]
                                 :drones)]
    result))

(defn add-route [{:keys [hops origin time]}]
  (let [result (transact->entity conn [{:route/origin origin}] :routes)
        {entity :entity
         tx     :tx} result
        tx @(d/transact conn (queries/mkroute (:db-after tx) hops (:db/id entity) time))
        entity (queries/one :routes (:db/id entity) (:db-after tx))
        result (assoc result :tx tx)
        result (assoc result :entity entity)]
    result))

(defn tryroute [{:keys [hops origin time]}]
  (let [id (d/tempid :db.part/user)
        tx-route (d/with (db) [{:db/id        id
                                :route/origin origin}])
        db-route (:db-after tx-route)
        tempids-route (:tempids tx-route)
        real-id (d/resolve-tempid db-route tempids-route id)
        tx (d/with db-route (queries/mkroute db-route hops real-id time))
        db (:db-after tx)]
    (queries/one :routes real-id db)))

(defn add-order [{:keys [shopid customerid route source]}]
  (let [result (transact->entity conn
                                 [{:order/shop     shopid
                                   :order/customer customerid
                                   :order/route    route
                                   :order/source   source}]
                                 :orders)]
    result))

(defn add-drone-type [{:keys [name range speed chargetime default]}]
  (let [result (transact->entity conn
                                 [{:dronetype/name       name
                                   :dronetype/range      range
                                   :dronetype/speed      speed
                                   :dronetype/chargetime chargetime
                                   :dronetype/default    default}]
                                 :dronetypes)]

    result))

(defn delete [id]
  @(d/transact conn
               [[:db.fn/retractEntity id]]))

(defn set-demand [{:keys [id demand]}]
  @(d/transact conn
               [{:db/id       id
                 :hive/demand demand}])
  demand)

(defn departure [time hopid]
  (let [db (d/db conn)
        hop (queries/one :hops hopid db)
        hiveid (:hop/start hop)
        drones (queries/drones-for-hive (:db/id hiveid) db)
        drones-with-charge (map
                             #(assoc % :charge (queries/charge-at-time (:db/id %) time db))
                             drones)
        as (println (map #(:drone/type %) drones-with-charge))
        sorted-drones-with-charge (sort-by :charge drones-with-charge)
        sorted-capable-drones (filter
                                #(do
                                   (println "AAAAA" %)
                                   (util/reachable-with-charge (:hop/distance hop)
                                                               (:dronetype/range (queries/one :dronetypes (:db/id (:drone/type %)) db))
                                                               (:charge %)))
                                sorted-drones-with-charge)]
    (if (empty? sorted-capable-drones)
      nil
      (let [selected-drone (last sorted-capable-drones)
            charge-after-hop (- (:charge selected-drone) (util/used-charge (queries/one :dronetypes (:db/id (:drone/type selected-drone)) db) (:hop/distance hop)))]
        (d/transact conn [{:db/id         hopid
                           :hop/drone     (:db/id selected-drone)
                           :hop/endcharge charge-after-hop}])
        (d/transact conn [{:db/id        (:db/id selected-drone)
                           :drone/status :drone.status/flying}])))))

(defn arrival [hopid]
  (let [db (d/db conn)
        hop (queries/one :hops hopid db)
        hiveid (:hop/end hop)
        droneid (:db/id (:hop/drone hop))]
    (d/transact conn [{:db/id        droneid
                       :drone/hive   hiveid
                       :drone/status :drone.status/idle}])))

(defn give-drones [num-drones db]
  (let [hiveids (mapv #(:db/id %) (queries/all :hives [] db))]
    (doseq [i hiveids]
      (dotimes [n num-drones]
        (add-drone {:hiveid i
                    :name   (str "init-drone-" i "-" n)
                    :type   nil
                    :status :drone.status/idle})))))
