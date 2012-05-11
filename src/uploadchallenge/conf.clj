(ns uploadchallenge.conf
  (:use
   [clojure.core]))

(def settings)

(defn initialize-config!
  [config-file]
  (defonce settings (merge (read-string (slurp config-file)))))


