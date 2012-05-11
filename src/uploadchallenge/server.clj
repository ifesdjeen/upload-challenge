(ns uploadchallenge.server
  (:use
   clojure.core
   ring.adapter.jetty
   clojure.tools.cli
   ring.middleware.multipart-params)

  (:require [uploadchallenge.file-processor :as file-processor]
            [uploadchallenge.request        :as request]
            [uploadchallenge.routing        :as routing]
            [uploadchallenge.conf           :as conf]

            [clojure.data.json         :as json]
            [ring.middleware.params    :as params]
            [ring.util.response        :as response]
            [ring.middleware.resource  :as resource]
            [ring.middleware.file-info :as file-info]

            [clostache.parser :as clstch]
            [clojure.java.io  :as io]))

;;
;; Route Handlers
;;

(defn update-description
  "POST /blobs/:filename

  Updates Description for the given file"
  []
  (let [req        (request/ring-request)
        description (get-in req [:params "description"])
        filename   (routing/extract-filename (:request-method req) (:uri req))
        file-info  (file-processor/update-file filename :description description) ]
    (-> (response/response (json/json-str (file-processor/get-file filename)))
        (response/content-type "application/javascript"))))

(defn status
  "GET /blobs/:filename

  Returns JSON hash with current file status, usually looks like:

      {\"tmp-file-name\":\"2.png\",\"filename\":\"2.png\",\"size\":998677,\"uploaded-size\":998491}

  "
  []
  (let [req           (request/ring-request)
        filename      (routing/extract-filename (:request-method req) (:uri req))
        file-info     (file-processor/get-file filename) ]
    (-> (response/response (json/json-str file-info))
        (response/content-type "application/javascript"))))

(defn file
  "GET /files/:filename

   Streams the file back to the client. Content-type 'application/force-download' is always used.
   In order to implement mime type determinition, refer to https://github.com/michaelklishin/pantomime
   or directly to Apache Tika.
  "
  []
  (let [req             (request/ring-request)
        filename        (routing/extract-filename (:request-method req) (:uri req))
        file-info       (file-processor/get-file filename)
        tmp-file        (io/file (:tmp-file-name file-info))]
    (-> (response/file-response (.getName tmp-file) {:root (str (.getParent tmp-file)) })
        (response/content-type "application/force-download"))))

(defn js
  "GET /javascripts/:filename.js

   Returns javascript resource, sets 'application/javascript' content-type.
  "
  []
  (let [req      (request/ring-request)
        filename (routing/extract-filename (:request-method req) (:uri req))]
    (-> (response/response (io!
                            (slurp (str (get-in conf-settings [:storage :resource-path]) "javascripts/" filename ".js"))))
        (response/content-type "application/javascript"))))

(defn blobs
  "GET /blobs

   Renders list of currently present blobs in Clostache.
  "
  []
  (let [files (vec (vals (file-processor/get-files)))]
    (-> (response/response (io!
                            (clstch/render (str (get-in conf/settings [:storage :resource-path]) "templates/blobs.html.clostache") {:files (vec files)})))
        (response/content-type "text/html"))))

(defn index
  "GET /blobs

   Renders pure HTML index page.
  "
  []
  (-> (response/response (io!
                          (slurp (str (get-in conf/settings [:storage :resource-path]) "templates/index.html"))))
      (response/content-type "text/html")))

;;
;; Server and handling initialization
;;

(defn handler
  "Default route-matching handler."
  [params]
  (let [req (request/ring-request)]
    (routing/match-route (:request-method req) (:uri req))))

(def app
  "Main Application endpoint, sets up used Ring Middleware."
  (-> handler
      (wrap-multipart-params { :store (file-processor/streamed-file-store) }) ;; use custom processor to persist upload status
      (request/wrap-request-map)
      (resource/wrap-resource "public")
      (params/wrap-params)
      (file-info/wrap-file-info)))


(defn -main
  "Routes, initializers and fun!"
  [& args]
  (let [ [options] (cli args ["--config" "Configuration file to use" :default "config/development.clj"])]

    (conf/initialize-config! (:config options))

    (routing/add-route :get "/favicon.ico" #())
    (routing/add-route :get "/" index)
    (routing/add-route :get "/blobs" blobs)
    (routing/add-route :post "/blobs" index)
    (routing/add-route :get "/files/(.*)" file)
    (routing/add-route :get "/blobs/(.*)" status)
    (routing/add-route :post "/blobs/(.*)" update-description)
    (routing/add-route :get "/javascripts/(.*).js" js)

    (run-jetty app (:http conf/settings))))