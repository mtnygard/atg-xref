(ns webui.module
  (:use [webui search nav component]
        clojure.contrib.json
        [clojure.java.io :only (file)]))

(defn module-components
  [qname]
  (map component-link (solr-query (str "+module:" qname " +component:*"))))

(defn module-link [m] {:link (str "/modules/" (:name m)) :name (:name m)})

(defn required-by
  [{reqd :required}]
  (if (coll? reqd) reqd (list reqd)))

(defn modules-named [pat] (solr-query (str "name:" pat)))

(defn modules-crumbs [] (conj (home-crumbs) (crumb "/modules" "Modules")))

(defn module-crumbs [mod] (conj (modules-crumbs) (crumb (str "/modules/" mod) mod)))

(defn- link-to
  [m]
  (let [mname (:name m)
        link (str "/modules/" mname)]
    [(str "<a href=\"" link "\">" mname "</a>")]))

(defn module-summaries
  [pat] (set (map link-to (modules-named pat))))

(defn modules-api
  [& pat] (json-str {:aaData (module-summaries (or pat "*"))}))

(defn module-page [qname]
  (let [mod (first (modules-named qname))]
    (if (empty? mod)
      nil
      {:breadcrumbs (module-crumbs qname) :body (view/module {:module mod})})))

(defn modules-page [] {:breadcrumbs (modules-crumbs)
                       :body (view/modules)})
