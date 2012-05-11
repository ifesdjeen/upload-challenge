(ns uploadchallenge.server
  (:use
   [clojure.core]
   [ring.adapter.jetty]
   [ring.middleware.multipart-params]
   )

  (:require [uploadchallenge.file-processor :as file-processor]
            [uploadchallenge.request        :as request]

            [clojure.data.json :as json]
            [ring.middleware.params :as params]
            [ring.util.response :as response]
            [ring.middleware.resource :as resource]
            [ring.middleware.file-info :as file-info]

            [clostache.parser :as clstch]
            [clojure.java.io :as io]))

(defonce routes (atom []))

(defn add-route [method uri handler]
  (swap! routes conj {:method method :uri uri :handler handler}))

(defn reset-routes!
  []
  (reset! routes []))

(defn find-route [method uri]
  (let [finder (fn [i]
                 (and (= (:method i) method)
                      (not (nil? (re-find (re-pattern (str "^" (:uri i) "$")) uri)))))]
    (first (filter finder @routes))))

(defn match-route [method uri]
  (let [route   (find-route method uri)
        handler (:handler route)]
    (if (nil? handler)
      (println "Can't find handler for:" method uri (request/ring-request))
      (handler))))

(defn extract-filename
  [method uri]
  (let [route   (find-route method uri)
        pattern (re-pattern (:uri route))
        matches (re-find pattern uri)]
    (second matches)))

(defn update-description []
  (let [req        (request/ring-request)
        description (get-in req [:params "description"])
        filename   (extract-filename (:request-method req) (:uri req))
        file-info  (file-processor/update-file filename :description description) ]
    (-> (response/response (json/json-str (file-processor/get-file filename)))
        (response/content-type "application/javascript"))))


(defn status []
  (let [req           (request/ring-request)
        filename      (extract-filename (:request-method req) (:uri req))
        file-info     (file-processor/get-file filename) ]
    (-> (response/response (json/json-str file-info))
        (response/content-type "application/javascript"))))

(defn file []
  (let [req             (request/ring-request)
        filename        (extract-filename (:request-method req) (:uri req))
        file-info       (file-processor/get-file filename)
        tmp-file        (io/file (:tmp-file-name file-info))]
    (-> (response/file-response (.getName tmp-file) {:root (str (.getParent tmp-file)) })
        (response/content-type "application/force-download"))))


(defn js []
  (let [req      (request/ring-request)
        filename (extract-filename (:request-method req) (:uri req))]
    (-> (response/response (io!
                            (slurp (io/resource (str "javascripts/" filename ".js")))))
        (response/content-type "application/javascript"))))

(defn blobs []
  (let [files (vec (vals (file-processor/get-files)))]
    (-> (response/response (io!
                            (clstch/render-resource "templates/blobs.html.clostache" {:files (vec files)})))
        (response/content-type "text/html"))))

(defn index []
  (-> (response/response (io!
                          (slurp (io/resource "templates/index.html"))))
      (response/content-type "text/html")))

(defn handler [params]
  (let [req (request/ring-request)]
    (match-route (:request-method req) (:uri req))))

(def app
  (-> handler
      (wrap-multipart-params { :store (file-processor/streamed-file-store) })
      (request/wrap-request-map)
      (resource/wrap-resource "public")
      (params/wrap-params)
      (file-info/wrap-file-info)))


(defn -main
  [& args]
  (add-route :get "/favicon.ico" #())
  (add-route :get "/" index)
  (add-route :get "/blobs" blobs)
  (add-route :post "/blobs" index)
  (add-route :get "/files/(.*)" file)
  (add-route :get "/blobs/(.*)" status)
  (add-route :post "/blobs/(.*)" update-description)
  (add-route :get "/javascripts/(.*).js" js)
  (run-jetty app {:port 8080}))