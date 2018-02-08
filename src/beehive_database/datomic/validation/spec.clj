(ns beehive-database.datomic.validation.spec
  (:require [clojure.spec.alpha :as spec]))

(spec/def :validation/address string?)
(spec/def :validation/xcoord float?)
(spec/def :validation/ycoord float?)
(spec/def :validation/name string?)
(spec/def :validation/hiveid int?)
(spec/def :validation/shopid int?)
(spec/def :validation/customerid int?)
(spec/def :validation/droneid int?)
(spec/def :validation/start int?)
(spec/def :validation/end int?)
(spec/def :validation/route int?)
(spec/def :validation/range float?)
(spec/def :validation/status keyword?)
(spec/def :validation/hops coll?)
(spec/def :validation/origin keyword?)
(spec/def :validation/speed float?)
(spec/def :validation/chargetime float?)
(spec/def :validation/default boolean?)
(spec/def :validation/time number?)
(spec/def :validation/source keyword?)

(spec/def :validation/hive
  (spec/keys :req-un [:validation/address :validation/xcoord :validation/ycoord :validation/name]))

(spec/def :validation/drone
  (spec/keys :req-un [:validation/hiveid :validation/name :validation/status]
             :opt-un [:validation/type]))

(spec/def :validation/route
  (spec/keys :req-un [:validation/hops :validation/origin :validation/time]))

(spec/def :validation/order
  (spec/keys :req-un [:validation/shopid :validation/customerid :validation/route :validation/source]))

(spec/def :validation/building
  (spec/keys :req-un [:validation/address :validation/xcoord :validation/ycoord]))

(spec/def :validation/shop
  (spec/keys :req-un [:validation/address :validation/xcoord :validation/ycoord :validation/name]))

(spec/def :validation/customer
  (spec/keys :req-un [:validation/address :validation/xcoord :validation/ycoord :validation/name]))

(spec/def :validation/hop
  (spec/keys :req-un [:validation/droneid :validation/start :validation/end]))

(spec/def :validation/dronetype
  (spec/keys :req-un [:validation/name :validation/range :validation/speed :validation/chargetime :validation/default]))

(spec/def :validation/tryroute
  (spec/keys :req-un [:validation/hops]))