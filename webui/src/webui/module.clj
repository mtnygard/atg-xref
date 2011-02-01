(ns webui.module
  (:use [webui search nav component])
  (:require view))

(defn module-components
  [qname]
  (map component-link (solr-query (str "+module:" qname " +component:*"))))

(defn modules-from-solr [query] (map ))

(defn module-link [m] {:link (str "/modules/" (:name m)) :name (:name m)})

(defn modules-named [pat] (solr-query (str "name:" pat)))

(defn modules-crumbs [] (conj (home-crumbs) (crumb "/modules" "Modules")))

(defn module-crumbs [mod] (conj (modules-crumbs) (crumb (str "/modules/" mod) mod)))

(defn module-page [qname] (view/module {:module (module-properties qname) :components (module-components qname)}))

(defn modules-page [] (view/modules (map module-link (modules-named "*"))))

(defn links-to-all-modules [] (map module-link (modules-named "*")))
