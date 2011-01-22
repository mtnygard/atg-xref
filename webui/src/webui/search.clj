(ns webui.search
  (:import [org.apache.solr.client.solrj SolrServer SolrQuery])
  (:import [org.apache.solr.client.solrj.response QueryResponse])
  (:import [org.apache.solr.client.solrj.impl CommonsHttpSolrServer]))

(def solr-url "http://localhost:8983/solr")

(def solr-server (ref {}))

(defn initialize-solr [] (dosync (ref-set solr-server (CommonsHttpSolrServer. solr-url))))

(defn query-results [qry]
  (.getResults (.query @solr-server qry)))

(defn modules-matching [pat]
  (query-results
   (-> (SolrQuery. (str "name:" pat))
       (.setRows Integer/MAX_VALUE))))
