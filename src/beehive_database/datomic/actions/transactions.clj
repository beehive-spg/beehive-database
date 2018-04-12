(ns beehive-database.datomic.actions.transactions
  (:require [datomic.api :as d]
            [beehive-database.datomic.actions.data :refer :all]
            [beehive-database.datomic.actions.queries :as queries]
            [beehive-database.datomic.actions.tx-generators :as gen]))

(defn transact->entity [conn data table]
  (let [id (d/tempid :db.part/user)
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
                                                      :hive/demand 5.0}}]
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
        tx @(d/transact conn (gen/gen-hops (:db-after tx) hops (:db/id entity) time))
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
        tx (d/with db-route (gen/gen-hops db-route hops real-id time))
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
        hop (queries/one :hops hopid db)]
    (d/transact conn [{:db/id        (:hop/drone hop)
                       :drone/hive   (:hop/end hop)
                       :drone/status :drone.status/flyings}])))

(defn arrival [hopid]
  (let [db (d/db conn)
        hop (queries/one :hops hopid db)
        droneid (:db/id (:hop/drone hop))]
    @(d/transact conn [{:db/id        droneid
                        :drone/status :drone.status/idle}])))

(defn give-drones [num-drones db]
  (let [hiveids (mapv #(:db/id %) (queries/all :hives [] db))]
    (doseq [i hiveids]
      (dotimes [n num-drones]
        (add-drone {:hiveid i
                    :name   (str "init-drone-" i "-" n)
                    :type   nil
                    :status :drone.status/idle})))))

