(ns indexer.core
  (:use atg.module
        indexer.jsp
        [clojure.contrib.io :only (as-file)])
  (:require [clojure.contrib.str-utils2 :as str]
            [clojure.contrib.logging :as log])
  (:import [java.io File FileNotFoundException]
           [org.apache.solr.client.solrj SolrServer]
           [org.apache.solr.common SolrInputDocument]
           [org.apache.solr.client.solrj.impl CommonsHttpSolrServer])
  (:gen-class))

(def *solr-server*)

(defn initialize-solr
  "Make a connection to Solr."
  [url]
  (CommonsHttpSolrServer. url))

(defmacro with-connection
  "Causes the body to be executed with a Solr connection bound to *solr-server*."
  [solr-url & body]
  `(binding [*solr-server* (initialize-solr ~solr-url)]
     ~@body))

(def nilsafe-split (fnil str/split ""))

(defn map->solr-input
  "Convert a map into a SolrJ object. (SolrInputDocument, to be precise.) Any map value that is a collection gets treated as a multivalued field. If this doesn't match the Solr schema, errors will result later when you add this document to Solr."
  [m]
  (let [doc (SolrInputDocument.)]
    (doseq [[k v] m]
      (if-not (nil? v)
        (if (coll? v)
          (doseq [subval v]
            (.addField doc (name k) subval))
          (.addField doc (name k) v))))
    doc))

(defn document-for-jsp
  "Return a map for a single JSP file"
  [mod webm j]
  (let [jsp (parse-jsp mod webm j)]
    {:id (str (:qname mod) ":" webm ":" (jsp-name j))
     :module (:qname mod)
     :name (jsp-name j)
     :body (:body jsp)
     :source (str (jsp-file j))
     :references (:references jsp)
     :pageImports (:page-imports jsp)
     :startCodeTag (:start-code-tag jsp)
     :endCodeTag (:end-code-tag jsp)
     })
  )

(defn jsp-documents
  "Return a lazy seq of maps for the JSP documents"
  [mod webm]
  (map #(document-for-jsp mod webm %) (web-module-jsps webm)))

(defn document-for-component
  "Return a map for a single component (i.e., Nucleus bean)"
  [mod sect compf]
  (let [comp (parse-component mod sect compf)]
    {:id (str (:qname mod) ":" (:section comp) ":" (:name comp))
     :module (:qname mod)
     :component (:name comp)
     :instantiates (:$class comp)
     :scope (:$scope comp)
     :body (:body comp)
     :source (:path comp)
     :references (:references comp)}))

(defn component-documents
  "Return a lazy seq of maps for all the components in a module"
  [m sect]
  (map #(document-for-component m sect %) (component-names m sect)))

(defn document-for-module
  "Return a map of attributes for a single ATG module"
  [manifest]
  {:id (:qname manifest)
   :name (:qname manifest)
   :product (:ATG-Product manifest)
   :required (nilsafe-split (:ATG-Required manifest) #" ")
   :classpath (nilsafe-split (:ATG-Class-Path manifest) #" ")
   :configpath (nilsafe-split (:ATG-Config-Path manifest) #" ")})

(defn module-documents
  "Return a lazy seq of maps for the module. The seq will always contain exactly 1 map."
  [m] (list (document-for-module m)))

(defn index-documents
  "Index a sequence of solr documents, logging some status as a side-effect"
  [docs & srcs]
  (log/info (str "Indexing " (count docs) " documents from " srcs))
  (doseq [d docs]
    (if (not (nil? (:id d)))
      (.add *solr-server* (map->solr-input d)))))

(defn index-modules
  "Index a collection of modules. Returns nothing of use. All indexing is a state-changing side-effect."
  [ms]
  (doseq [m ms]
    (index-documents (module-documents m) (:qname m))
    (doseq [sect ["config" "liveconfig" "cacheconfig"]]
      (index-documents (component-documents m sect) (:qname m) sect))
    (doseq [webm (web-modules m)] 
      (index-documents (jsp-documents m webm) (:qname m) (web-module-name webm))))
  (.commit *solr-server*))

(defn -main
  "Index modules found under 'root', running on a single thread and using a single Solr connection."
  [root]
  (with-connection "http://localhost:8983/solr"
    (index-modules (load-modules root))))
