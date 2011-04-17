(ns webui.component
  (:use webui.nav
        webui.search
        clojure.contrib.json)
  (:require view))

(defn components-crumbs
  "Get a breadcrumb trail for the components page itself."
  []
  (conj (home-crumbs) (crumb "/components" "Components")))

(defn component-crumbs
  "Get a breadcrumb trail for a single component's own page."
  [comp]
  (conj (components-crumbs) (crumb (str "/component/" comp) comp)))

(defn components-api
  "Return all components known to Solr, as a single JSON object. Data is an array, under the key 'aaData'."
  []
  (json-str {:aaData
             (partition 1 (set (map :component (solr-query "component:*"))))}))

(defn components-in-module
  "Return the components within the named module. Data is an array, under the key 'aaData'."
  [pat]
  (json-str {:aaData
             (partition 1 (set (map :component (solr-query (str "component:* +module:" pat)))))}))

(defn component-page
  "View function to render a page for a single component. Returns a map suitable for passing to layout-or-404."
  [comp]
  (if-let [defs (solr-query (str "component:" comp))]
    {:breadcrumbs (component-crumbs comp)
     :body (let [references (set (flatten (map :references defs)))]
             (view/component {:name comp
                              :component-defs defs
                              :uses references}))}
    nil))

(defn components-page
  "View function to render the components page. Returns a map suitable for passing to layout-or-404."
  []
  {:breadcrumbs (components-crumbs)
   :body (view/components)})

