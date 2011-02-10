(ns webui.module
  (:use [webui search nav component]
        clojure.contrib.json)
  (:require view))

(defn module-components
  [qname]
  (map component-link (solr-query (str "+module:" qname " +component:*"))))

(defn module-link [m] {:link (str "/modules/" (:name m)) :name (:name m)})

(defn modules-named [pat] (solr-query (str "name:" pat)))

(defn modules-crumbs [] (conj (home-crumbs) (crumb "/modules" "Modules")))

(defn module-crumbs [mod] (conj (modules-crumbs) (crumb (str "/modules/" mod) mod)))

(defn module-page [qname] (view/module {:module (first (modules-named qname)) :components (module-components qname)}))

(defn modules-page [] (view/modules (map module-link (modules-named "*"))))

(defn- link-to
  [m]
  (let [mname (:name m)
        link (str "/modules/" mname)]
    [(str "<a href=\"" link "\">" mname "</a>")]))

(defn module-summaries
  [pat] (set (map link-to (modules-named pat))))

(defn modules-api
  [& pat] (json-str {:aaData (module-summaries (or pat "*"))}))
