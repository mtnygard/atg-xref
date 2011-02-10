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

(defn components-page [] (view/components (map component-link (components-named "*"))))

(defn- link-to
  [m]
  (let [cname (:component m)
        link (str "/component/" cname)]
    [(str "<a href=\"" link "\">" cname "</a>")]))

(defn component-summaries
  [pat] (set (map link-to (components-named pat))))

(defn components-api
  [& pat] (json-str {:aaData (component-summaries (or pat "*"))}))

(defn component-page
  [comp]
  (let [definitions (components-named comp)
        references (set (flatten (map :references definitions)))]
    (view/component {:name comp
                     :component-defs definitions
                     :uses (map component-link references)})))

