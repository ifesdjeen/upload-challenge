(ns uploadchallenge.file-processor
  (:import (java.io File InputStream))
  (:use
   [clojure.core])
  (:require [uploadchallenge.request        :as request]
            [uploadchallenge.conf           :as conf]
            [clojure.java.io :as io]))

(defonce files (atom {}))

(defn add-file
  "Add file to in-memory Atom store"
  [id filename size]
  (swap! files assoc id {:blobid id :filename filename :size size :uploaded-size 0}))

(defn remove-file
  "Removes file from in-memory Atom store"
  [id size]
  (swap! files dissoc id))

(defn get-file
  "Returns frile by filename, from in-memory Atom store"
  [id]
  (get @files id))

(defn get-files
  "Get all the files from Atom"
  []
  @files)

(defn update-file
  "Updates file uploaded size when given, puts merged hash to Atom

   EXAMPLES:

         (update-file (:filename item) :tmp-file-name (.getPath temp-file)) ;; doesn't change uploaded-size

         (update-file (:filename item) :uploaded-size bytes) ;; adds 'bytes' to current uploaded size
   "
  ([id & {:keys [uploaded-size description tmp-file-name] :or {uploaded-size 0} :as opts}]
     (let [old-file           (get-file id)
           uploaded-size      (+ uploaded-size (:uploaded-size old-file))
           discard-nils       (apply dissoc opts (for [[k v] opts :when (nil? v)] k))
           with-uploaded-size (merge old-file (assoc discard-nils :uploaded-size uploaded-size))]
       (swap! files assoc id with-uploaded-size))))

(defn- ^File make-temp-file [filename file-set]
  "Makes a temp file in a configured directory"
  (let [temp-file (File. (get-in conf/settings [:storage :tmp-file-path]) filename)]
    (swap! file-set conj temp-file)
    (.deleteOnExit temp-file)
    temp-file))


(defn streamed-file-store
  "Stream file, save upload progress and persist its contents"
  ([& keys]
     (fn [item]
       (let [req            (request/ring-request)
             file-upload-id (get-in req [:query-params "blobid"])
             file-set       (atom #{})
             temp-file      (make-temp-file (:filename item) file-set)
             stream         (:stream item)]

         (add-file file-upload-id (:filename item) (:content-length (request/ring-request)))

         (io/copy (proxy [InputStream] []
                     (read [buffer]
                       (let [bytes (.read stream buffer)]
                         (update-file file-upload-id :uploaded-size bytes)
                         bytes))) temp-file)

         (update-file file-upload-id :tmp-file-name (.getPath temp-file))
         (-> (select-keys item [:filename :content-type])
             (assoc :tempfile temp-file
                    :size (.length temp-file)))))))
