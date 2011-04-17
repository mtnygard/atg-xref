(ns webui.classes
  (:use [webui nav search]
        clojure.contrib.json)
  (:require view))

(defn class-link
  [c]
  {:link (str "/classes/" c) :name c})

(defn classes-page [] (view/classes))

(defn classes-named [pat] (solr-query (str "classname:" pat)))

(defn classes-crumbs [] (conj (home-crumbs) (crumb "/classes" "Classes")))

(defn class-crumbs [c] (conj (classes-crumbs) (crumb (str "/classes/" c) c)))

(defn classes-page [] {:breadcrumbs (classes-crumbs)
                       :body (view/classes)})

(defn classes-api
  "Return JSON representing names of all classes known to Solr. These will be all classes instantiated by a component, not necessarily all classes defined in source."
  []
  (json-str {:aaData (partition 1 (set (map :instantiates (solr-query "instantiates:*"))))}))

(defn instantiated-by
  "Return a collection of components that instantiate the named class"
  [c]
  (set (map :component (solr-query (str "instantiates:\"" c "\"")))))

(defn class-page
  [c]
  {:breadcrumbs (class-crumbs c)
   :body (view/a-class {:name c
                        :uses (instantiated-by c)})})

