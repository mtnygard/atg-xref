(ns indexer.core
  (:use atg.module
        [clojure.contrib.io :only (as-file)])
  (:require clojure.contrib.str-utils2)
  (:import [java.io File FileNotFoundException]
           [org.apache.solr.client.solrj SolrServer]
           [org.apache.solr.common SolrInputDocument]
           [org.apache.solr.client.solrj.impl CommonsHttpSolrServer])
  (:gen-class))

(def *solr-server*)

(defn initialize-solr
  [url]
  (CommonsHttpSolrServer. url))

(defmacro with-connection
  [solr-url & body]
  `(binding [*solr-server* (initialize-solr ~solr-url)]
     ~@body))

(defn nilsafe-split [s re] (if (nil? s) "" (clojure.contrib.str-utils2/split s re)))

(defn map->solr-input
  [m]
  (let [doc (SolrInputDocument.)]
    (doseq [[k v] m]
      (if-not (nil? v)
        (if (coll? v)
          (doseq [subval v]
            (.addField doc (name k) subval))
          (.addField doc (name k) v))))
    doc))

(defn document-for-component
  [mod sect compf]
  (let [comp (parse-component mod sect compf)]
    (map->solr-input
     {:id (str (:qname mod) ":" (:section comp) ":" (:name comp))
      :module (:qname mod)
      :component (:name comp)
      :classname (:$class comp)
      :scope (:$scope comp)})))

(defn index-components
  [m]
  (for [sect ["config" "liveconfig"]]
    (let [components (component-names m sect)]
      (println "indexing" (count components) "from" (:qname m) "[" sect "]")
      (doseq [c components]
        (.add *solr-server* (document-for-component m sect c))))))

(defn document-for-module
  [manifest]
  (map->solr-input
   {:id (:qname manifest)
    :name (:qname manifest)
    :product (:ATG-Product manifest)
    :required (nilsafe-split (:ATG-Required manifest) #" ")
    :classpath (nilsafe-split (:ATG-Class-Path manifest) #" ")
    :configpath (nilsafe-split (:ATG-Config-Path manifest) #" ")}))

(defn index-modules
  [ms]
  (doseq [m ms]
    (.add *solr-server* (document-for-module m))
    (doall (index-components m)))
  (.commit *solr-server*))

(defn -main [root]
  (with-connection "http://localhost:8983/solr"
    (index-modules (load-modules root))))
