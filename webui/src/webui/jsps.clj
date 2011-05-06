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
  (conj (jsps-crumbs) (crumb (str "/jsp/" jsp) jsp)))

(defn jsps-api
  "Get a JSON string with a tuple of {name, start tag, end tag, # of beans} for each JSP"
  []
  (json-str {:aaData
             (map (juxt :name :startCodeTag :endCodeTag (comp count :references))
                  (solr-query "type:jsp"))}))

(defn jsps-page
  "View function to render the page with all JSPs. Returns a map suitable for passing to layout-or-404"
  []
  {:breadcrumbs (jsps-crumbs)
   :body (view/jsps)})

(defn jsp-outrefs
  [f defs]
  (set (flatten (filter (comp not nil?) (map f defs)))))

(defn jsp-page
  "View function to render a page for a single JSP. Returns a map suitable for passing to layout-or-404"
  [name]
  (if-let [defs (solr-query (str "type:jsp && name:\"" name "\""))]
    {:breadcrumbs (jsp-crumbs name)
     :body (let [component-references (jsp-outrefs :references defs)
                 page-references (jsp-outrefs :pageImports defs)]
             (view/jsp {:name name
                        :jsp-defs defs
                        :bean-uses component-references
                        :jsp-uses page-references
                        }))}
    nil
    ))
