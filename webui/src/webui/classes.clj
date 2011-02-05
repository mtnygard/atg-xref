(ns webui.classes
  (:use webui.nav
        webui.search)
  (:require view))

(defn class-link
  [c]
  {:link (str "/classes/" c) :name c})

(defn classes-page [] (view/classes))

(defn classes-named [pat] (solr-query (str "classname:" pat)))

(defn classes-crubms [] (conj (home-crumbs) (crumb "/classes" "Classes")))

(defn class-crumbs [c] (conj (classes-crumbs) (crumb (str "/classes/" c) c)))

(defn classes-page [] (view/classes (map class-link (take 35 (classes-named "*")))))

(defn class-page
  [c]
  (let [definitions (classes-named c)
        references (set (flatten (map :references definitions)))]
    (view/a-class {:name c
                   :class-definitions definitions
                   :uses (map class-link references)})))

(defn links-to-top-classes [] (map class-link (take 35 (classes-named "*"))))
