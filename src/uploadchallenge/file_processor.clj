(ns uploadchallenge.file-processor
  (:import (java.io File InputStream))
  (:use
   [clojure.core])
  (:require [uploadchallenge.request        :as request]
            [clojure.java.io :as io]))

(defonce files (atom {}))

(defn add-file [filename size]
  (swap! files assoc filename {:filename filename :size size :uploaded-size 0}))

(defn remove-file [filename size]
  (swap! files dissoc filename))

(defn get-file [filename]
  (get @files filename))

(defn get-files []
  @files)

(defn update-file
  ;; ([filename uploaded-size]
  ;;    (update-file filename uploaded-size "" ""))
  ;; ([filename uploaded-size description]
  ;;    (update-file filename uploaded-size description ""))
  ([filename & {:keys [uploaded-size description tmp-file-name] :or {uploaded-size 0} :as opts}]
     (let [old-file          (get-file filename)
           uploaded-size      (+ uploaded-size (:uploaded-size old-file))
           discard-nils       (apply dissoc opts (for [[k v] opts :when (nil? v)] k))
           with-uploaded-size (merge old-file (assoc discard-nils :uploaded-size uploaded-size))] ;; :description description :tmp-file-name tmp-file-name)]
           ;; with-uploaded-size (assoc old-file :uploaded-size uploaded-size :description description :tmp-file-name tmp-file-name)]
       ;; (println (* 100 (float (/ uploaded-size (:size old-file)))) "%")
       (swap! files assoc filename with-uploaded-size))))

(defn- ^File make-temp-file [filename file-set]
  ;; (let [temp-file (File/createTempFile "multipart-" nil)]
  (let [temp-file (File. "/Users/alexp/store" filename)]
    (swap! file-set conj temp-file)
    (.deleteOnExit temp-file)
    temp-file))


(defn streamed-file-store
  ([& keys]
     (fn [item]
       (let [file-set  (atom #{})
             temp-file (make-temp-file (:filename item) file-set)
             stream    (:stream item)]

         (add-file (:filename item) (:content-length (request/ring-request)))

         (io/copy (proxy [InputStream] []
                     (read [buffer]
                       (let [bytes (.read stream buffer)]
                         (update-file (:filename item) :uploaded-size bytes)
                         bytes))) temp-file)

         (update-file (:filename item) :tmp-file-name (.getPath temp-file))
         (-> (select-keys item [:filename :content-type])
             (assoc :tempfile temp-file
                    :size (.length temp-file)))))))
