(ns webui.search
  (:import [org.apache.solr.client.solrj SolrServer SolrQuery])
  (:import [org.apache.solr.client.solrj.response QueryResponse])
  (:import [org.apache.solr.client.solrj.impl CommonsHttpSolrServer]))

(def *solr-server*)

(defn initialize-solr
  [url]
  (CommonsHttpSolrServer. url))

(defmacro with-connection
  [solr-url & body]
  `(binding [*solr-server* (initialize-solr ~solr-url)]
     ~@body))

(defn wrap-solr
  [app url]
  (fn [req]
    (with-connection url
      (app req))))

(defn query-results [qry]
  (.getResults (.query *solr-server* qry)))

(defn modules-matching [pat]
  (query-results
   (-> (SolrQuery. (str "name:" pat))
       (.setRows Integer/MAX_VALUE))))
