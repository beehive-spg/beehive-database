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
(s/def :validation/status keyword?)
(s/def :validation/hops seq?)
(s/def :validation/origin keyword?)
(s/def :validation/speed float?)
(s/def :validation/chargetime float?)
(s/def :validation/default boolean?)

(s/def :validation/hive
  (s/keys :req [:validation/address :validation/xcoord :validation/ycoord :validation/name]))

(s/def :validation/drone
  (s/keys :req [:validation/hiveid :validation/name :validation/range :validation/status]))

(s/def :validation/route
  (s/keys :req [:validation/hops :validation/origin]))

(s/def :validation/order
  (s/keys :req [:validation/shopid :validation/customerid :validation/route]))

(s/def :validation/building
  (s/keys :req [:validation/address :validation/xcoord :validation/ycoord]))

(s/def :validation/shop
  (s/keys :req [:validation/address :validation/xcoord :validation/ycoord :validation/name]))

(s/def :validation/customer
  (s/keys :req [:validation/address :validation/xcoord :validation/ycoord :validation/name]))

(s/def :validation/hop
  (s/keys :req [:validation/droneid :validation/start :validation/end]))

(s/def :validation/dronetype
  (s/keys :req [:validation/name :validation/range :validation/speed :validation/chargetime :validation/default]))