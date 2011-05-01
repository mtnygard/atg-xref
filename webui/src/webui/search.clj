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

(defn solr-results->map
  [doc]
  (let [res (transient {})]
    (doseq [field (.getFieldNames doc)]
      (let [val (.getFieldValue doc field)]
        (if (instance? java.util.List val)
          (assoc! res (keyword field) (vec val))
          (assoc! res (keyword field) val))))
    (persistent! res)))

(defn solr-query
  ([s n]
     (map solr-results->map (query-results
                             (-> (SolrQuery. s)
                                 (.setRows n)))))
  ([s]
     (solr-query s Integer/MAX_VALUE)))
