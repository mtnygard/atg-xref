(ns webui.component
  (:use webui.nav
        webui.search
        clojure.contrib.json)
  (:require view))

(defn component-link
  [m]
  (let [cname (if (string? m) m (:component m))]
    {:link (str "/component/" cname) :name cname}))

(defn components-named [pat] (solr-query (str "component:" pat)))

(defn components-crumbs [] (conj (home-crumbs) (crumb "/components" "Components")))

(defn component-crumbs [comp] (conj (components-crumbs) (crumb (str "/component/" comp) comp)))

(defn- link-to
  [m]
  (let [cname (:component m)
        link (str "/component/" cname)]
    [(str "<a href=\"" link "\">" cname "</a>")]))

(defn component-summaries
  [f] (set (map link-to (f))))

(defn components-api
  [& pat] (json-str {:aaData (component-summaries #(components-named (or pat "*")))}))

(defn components-in-module
  [pat] (json-str {:aaData (component-summaries #(solr-query (str "component:* +module:" pat)))}))

(defn component-page
  [comp]
  (if-let [defs (components-named comp)]
    {:breadcrumbs (component-crumbs comp)
     :body (let [references (set (flatten (map :references defs)))]
             (view/component {:name comp
                              :component-defs defs
                              :uses (map component-link references)}))}
    nil))

(defn components-page
  []
  {:breadcrumbs (components-crumbs)
   :body (view/components (map component-link (components-named "*")))})

