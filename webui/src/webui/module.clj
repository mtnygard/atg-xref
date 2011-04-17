(ns webui.module
  (:use [webui search nav component]
        clojure.contrib.json
        [clojure.java.io :only (file)]))

(defn module-components
  [qname]
  (map component-link (solr-query (str "+module:" qname " +component:*"))))

(defn required-by
  [{reqd :required}]
  (if (coll? reqd) reqd (list reqd)))

(defn requiring
  [mod]
  (set (map :name (solr-query (str "required:\"" (:name mod) "\"")))))

(defn modules-named [pat] (solr-query (str "name:" pat)))

(defn modules-crumbs [] (conj (home-crumbs) (crumb "/modules" "Modules")))

(defn module-crumbs [mod] (conj (modules-crumbs) (crumb (str "/module/" mod) mod)))

(defn modules-api
  [& pat] (json-str {:aaData (partition 1 (set (map :name (solr-query "name:*"))))}))

(defn module-page [qname]
  (let [mod (first (modules-named qname))]
    (if (empty? mod)
      nil
      {:breadcrumbs (module-crumbs qname)
       :body (view/module {:module mod
                           :required-by (required-by mod)
                           :requiring (requiring mod)})})))

(defn modules-page [] {:breadcrumbs (modules-crumbs)
                       :body (view/modules)})
