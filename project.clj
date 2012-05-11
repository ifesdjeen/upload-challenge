(defproject uploadchallenge "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure   "1.3.0"]
                 [org.clojure/data.json "0.1.2"]
                 [ring                  "1.1.0"]
                 [de.ubercode.clostache/clostache "1.3.0"]]

  :resource-paths ["src/resources"]
  :main           uploadchallenge.server
  :ring {:handler uploadchallenge.server/app})
