(ns indexer.core
  (:use [atg manifest])
  (:use [clojure.contrib.io :only (as-file)])
  (:use [clojure.contrib.str-utils2 :only (split)])
  (:import [java.io File FileNotFoundException])
  (:import [org.apache.solr.client.solrj SolrServer])
  (:import [org.apache.solr.common SolrInputDocument])
  (:import [org.apache.solr.client.solrj.impl CommonsHttpSolrServer])
  (:gen-class))

(defn files-with-extension [^File dir extension]
  (filter #(.endsWith (.getPath %) extension) (file-seq dir)))

(def *solr-server*)

(defn initialize-solr
  [url]
  (CommonsHttpSolrServer. url))

(defmacro with-connection
  [solr-url & body]
  `(binding [*solr-server* (initialize-solr ~solr-url)]
     ~@body))

(defn split-field [m n] (split (get m n "") #" "))

(defn add-all [doc docn m n]
  (doall (map #(.addField doc docn %) (split-field m n))))

(defn module-components
  [ms]
  [])

(defn index-components
  [ms]
  #_(.add *solr-server* (map module-components ms))
  )

(defn map->solr-input
  [m]
  (let [doc (SolrInputDocument.)]
    (doseq [[k v] m]
      (if (coll? v)
        (doseq [subval v]
          (.addField doc (name k) subval))
        (.addField doc (name k) v)))
    doc))

(defn document-for-module
  [manifest]
  (map->solr-input
   {:id (get manifest :qname "")
    :name (get manifest :qname "")
    :product (get manifest "ATG-Product" "")
    :required (split-field manifest "ATG-Required")
    :classpath (split-field manifest "ATG-Class-Path")
    :configpath (split-field manifest "ATG-Config-Path")}))

(defn index-modules
  [ms]
  (.add *solr-server* (map document-for-module ms))
  (.commit *solr-server*))

(defn -main [root]
  (with-connection "http://localhost:8983/solr"
    (let [ms (load-modules root)]
      (index-modules ms)
      (index-components ms))))
