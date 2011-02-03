(ns webui.component
  (:use webui.nav
        webui.search)
  (:require view))

(defn component-link
  [m]
  (let [cname (if (string? m) m (:component m))]
    {:link (str "/component/" cname) :name cname}))

(defn components-named [pat] (solr-query (str "component:" pat)))

(defn components-crumbs [] (conj (home-crumbs) (crumb "/components" "Components")))

(defn component-crumbs [comp] (conj (components-crumbs) (crumb (str "/component/" comp) comp)))

(defn components-page [] (view/components (map component-link (take 35 (components-named "*")))))

(defn component-page
  [comp]
  (let [definitions (components-named comp)
        references (set (flatten (map :references definitions)))]
    (view/component {:name comp
                     :component-defs definitions
                     :uses (map component-link references)})))

(defn links-to-top-components [] (map component-link (take 35 (components-named "*"))))
