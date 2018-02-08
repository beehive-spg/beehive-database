(defproject beehive-database "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :repositories {"my.datomic.com" {:url      "https://my.datomic.com/repo"
                                   :username :env/USERNAME
                                   :password :env/PASSWORD}}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [com.datomic/datomic-pro "0.9.5656"]
                 [liberator "0.15.1"]
                 [compojure "1.6.0"]
                 [ring/ring-core "1.6.3"]
                 [ring/ring-jetty-adapter "1.6.3"]
                 [ring/ring-json "0.4.0"]
                 [datomic-schema "1.3.0"]]
  :profiles {:uberjar {:main beehive-database.core}}
  :main beehive-database.core)
;;:main ^:skip-aot beehive-database.core
:target-path "target/%s"
;;:profiles {:uberjar {:aot :all}}


