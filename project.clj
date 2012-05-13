(defproject uploadchallenge "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure   "1.3.0"]
                 [org.clojure/data.json "0.1.2"]
                 [ring                  "1.1.0"]
                 [de.ubercode.clostache/clostache "1.3.0"]
                 [org.clojure/tools.cli "0.2.1"] ;; That one may have been easily avoided :)
                 ]

  :profiles       {:deployment {} }
  :source-path    "src"
  :resource-paths ["src/resources"]
  ;; it's likely to still be a Leiningen bug, please refer to an ongoing discussion here: https://github.com/technomancy/leiningen/issues/31#issuecomment-5675428
  ;; Until it's fixed there, following line should be a part of project.clj for successfull uberjar and
  ;; deployment.
  :uberjar-exclusions [#"(?i)^META-INF/[^/]*\.SF$"]
  :aot            [uploadchallenge.server]
  :main           uploadchallenge.server)
