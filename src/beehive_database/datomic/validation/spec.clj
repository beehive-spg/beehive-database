(ns beehive-database.datomic.validation.spec
  (:require [clojure.spec.alpha :as s]))

(s/def :validation/address string?)
(s/def :validation/xcoord float?)
(s/def :validation/ycoord float?)
(s/def :validation/name string?)
(s/def :validation/hiveid int?)
(s/def :validation/shopid int?)
(s/def :validation/customerid int?)
(s/def :validation/droneid int?)
(s/def :validation/start int?)
(s/def :validation/end int?)
(s/def :validation/route int?)
(s/def :validation/range float?)
(s/def :validation/status string?)
(s/def :validation/hops seq?)
(s/def :validation/origin keyword?)
(s/def :validation/speed float?)
(s/def :validation/chargetime float?)
(s/def :validation/default boolean?)

(s/def :validation/hive
  (s/keys :req-un [:validation/address :validation/xcoord :validation/ycoord :validation/name]))

(s/def :validation/drone
  (s/keys :req-un [:validation/hiveid :validation/name :validation/range :validation/status]))

(s/def :validation/route
  (s/keys :req-un [:validation/hops :validation/origin]))

(s/def :validation/order
  (s/keys :req-un [:validation/shopid :validation/customerid :validation/route]))

(s/def :validation/building
  (s/keys :req-un [:validation/address :validation/xcoord :validation/ycoord]))

(s/def :validation/shop
  (s/keys :req-un [:validation/address :validation/xcoord :validation/ycoord :validation/name]))

(s/def :validation/customer
  (s/keys :req-un [:validation/address :validation/xcoord :validation/ycoord :validation/name]))

(s/def :validation/hop
  (s/keys :req-un [:validation/droneid :validation/start :validation/end]))

(s/def :validation/dronetype
  (s/keys :req-un [:validation/name :validation/range :validation/speed :validation/chargetime :validation/default]))

(s/def :validation/tryroute
  (s/keys :req-un [:validation/hops]))