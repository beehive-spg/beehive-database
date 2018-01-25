(ns beehive-database.datomic.validation.spec
  (:require [clojure.spec.alpha :as s]))

(s/def :hivespec/address string?)
(s/def :hivespec/xcoord float?)
(s/def :hivespec/ycoord float?)
(s/def :hivespec/name string?)

(s/def :hivespec/hive (s/keys :req [:hivespec/address :hivespec/xcoord :hivespec/ycoord :hivespec/name]))