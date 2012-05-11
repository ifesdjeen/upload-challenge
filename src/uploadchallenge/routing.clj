(ns uploadchallenge.routing
  (:use
   [clojure.core])
  (:require
      [uploadchallenge.request        :as request]))


(defonce routes (atom []))

(defn add-route
  "Adds route to the Route store for future matching

      ;; will match all the files with .js extension under /javascripts/ dir
      (routing/add-route :get \"/javascripts/(.*).js\" js)

      ;; matches an exact path
      (routing/add-route :post \"/blobs\" index)

  "
  [method uri handler]
  (swap! routes conj {:method method :uri uri :handler handler}))

(defn reset-routes!
  "Mostly for testing purposes, reset Routes table"
  []
  (reset! routes []))

(defn find-route
  "Find a route that matches the given request"
  [method uri]
  (let [finder (fn [i]
                 (and (= (:method i) method)
                      (not (nil? (re-find (re-pattern (str "^" (:uri i) "$")) uri)))))]
    (first (filter finder @routes))))

(defn match-route
  "Run a handler for a route that matches given request

  EXAMPLES:

    (def x (atom 0))
    (add-route :get \"/\" (fn []
                          (reset! x 20)))
    (match-route :get \"/\") ;; will run reset x atom to 20
    (match-route :get \"/bazinga\") ;; will not match


    (def x (atom 0))
    (add-route :get \"/javascripts/(.*).js\" (fn []
                                          (reset! x 20)))

    (match-route :get \"/javascripts/jquery.js\") ;; will reset x atom to 20
    (match-route :get \"/YABADABASCRIPTS/jquery.js\") ;; will not match

  "
  [method uri]
  (let [route   (find-route method uri)
        handler (:handler route)]
    (if (nil? handler)
      (println "Can't find handler for:" method uri (request/ring-request))
      (handler))))

(defn extract-filename
  "Extracts filename if given pattern looks like a regexp:


    (add-route :get \"/javascripts/(.*).js\" #())

    (extract-filename :get \"/javascripts/jquery.js\") ;; returns 'jquery'
  "
  [method uri]
  (let [route   (find-route method uri)
        pattern (re-pattern (:uri route))
        matches (re-find pattern uri)]
    (second matches)))

