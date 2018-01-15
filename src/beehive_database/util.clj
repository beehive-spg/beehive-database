(ns beehive-database.util)

(def earth-radius 6371.009)

(defn- degrees->radians [point]
  (mapv #(Math/toRadians %) point))

(defn distance
  ([p1 p2] (distance p1 p2 earth-radius))
  ([p1 p2 radius]
   (let [[lat1 long1] (degrees->radians p1)
         [lat2 long2] (degrees->radians p2)]
     (* radius
        (Math/acos (+ (* (Math/sin lat1) (Math/sin lat2))
                      (* (Math/cos lat1)
                         (Math/cos lat2)
                         (Math/cos (- long1 long2)))))))))

(def memo-distance
  (memoize distance))

(defn reachable [p1 p2 range]
  (let [distance (distance p1 p2)]
    (< distance range)))

(def memo-reachable
  (memoize reachable))

(defn get-pos [hive]
  [(:building/xcoord hive) (:building/ycoord hive)])