(ns beehive-database.util)

(def earth-radius 6371.009)

(defn- degrees->radians [point]
  (mapv #(Math/toRadians %) point))

(defn distance
  ([p1 p2] (distance p1 p2 earth-radius))
  ([p1 p2 radius]
   (let [[lat1 long1] (degrees->radians p1)
         [lat2 long2] (degrees->radians p2)]
     (* 1000
        radius
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

(defn position [building]
  [(:building/xcoord building) (:building/ycoord building)])

(defn travel-time [p1 p2 speed]
  (/
    (distance
      p1
      p2)
    speed))

(defn map-num [num in-min in-max out-min out-max]
  (+ (/ (* (- num in-min)
           (- out-max out-min))
        (- in-max in-min))
     out-min))

(defn reachable-with-charge [p1 p2 max-range charge-percent]
  (let [range (* max-range (/ charge-percent 100))]
    (reachable p1 p2 range)))