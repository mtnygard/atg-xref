(ns indexer.core
  (:use [atg module manifest])
  (:use [clojure.contrib.io :only (as-file)])
  (:use [clojure.contrib.str-utils2 :only (split)])
  (:import [java.io File FileNotFoundException])
  (:import [org.apache.solr.client.solrj SolrServer])
  (:import [org.apache.solr.common SolrInputDocument])
  (:import [org.apache.solr.client.solrj.impl CommonsHttpSolrServer])
  (:gen-class))

(def solr-url "http://localhost:8983/solr")

(defn files-with-extension [^File dir extension]
  (filter #(.endsWith (.getPath %) extension) (file-seq dir)))

(def solr-server (ref {}))

(defn initialize-solr [] (dosync (ref-set solr-server (CommonsHttpSolrServer. solr-url))))

(defn split-field [m n] (split (get m n "") #" "))

(defn add-all [doc docn m n]
  (doall (map #(.addField doc docn %) (split-field m n))))

(defn document-for-module
  [m]
  (doto (SolrInputDocument.)
    (.addField "id" (:qname m))
    (.addField "name" (:qname m))
    (.addField "product" (get m "ATG-Product" ""))
    (add-all "required" m "ATG-Required")
    (add-all "classpath" m "ATG-Class-Path")
    (add-all "configpath" m "ATG-Config-Path")))

(defn index-modules
  [ms]
  (.add @solr-server (map document-for-module ms))
  (.commit @solr-server))

(defn -main [root]
  (initialize-solr)
  (index-modules (load-modules root)))
