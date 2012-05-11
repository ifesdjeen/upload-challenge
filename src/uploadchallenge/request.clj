(ns uploadchallenge.request
  (:use
   [clojure.core]))

(declare ^{:dynamic true} *request*)

(defn ring-request
  []
  *request*)

(defn wrap-request-map [handler]
  (fn [req]
    (binding [*request* req]
      (handler req))))
