(ns webui.jsps
  (:use webui.nav
        webui.search
        clojure.contrib.json)
  (:require view))

(defn jsps-crumbs
  "Get a breadcrumb trail for the JSP overview page"
  []
  (conj (home-crumbs) (crumb "/jsps" "JSPs")))

(defn jsp-crumbs
  "Get a breadcrumb trail for a page with a single JSP"
  [jsp]
  (conj (jsps-crumbs) (crumb (str "/jsps/" jsp) jsp)))

(defn jsps-api
  "Get a JSON string with a tuple of {name, start tag, end tag, # of beans} for each JSP"
  []
  (let [reference-count (fn [{refs :references}]
                          (cond
                           (nil? refs) 0
                           (coll? refs) (count refs)
                           :else 1))]
    (json-str {:aaData
               (map (juxt :name :startCodeTag :endCodeTag reference-count)
                    (solr-query "type:jsp"))})))

(defn jsps-page
  "View function to render the page with all JSPs. Returns a map suitable for passing to layout-or-404"
  []
  {:breadcrumbs (jsps-crumbs)
   :body (view/jsps)})
