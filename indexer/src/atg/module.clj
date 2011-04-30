(ns atg.module
  (:use [clojure.contrib.io :only (file-str input-stream)]
        [clojure.java.io :only (file)])
  (:require [clojure.string :as str])
  (:import (java.io FileInputStream)
           (java.util.jar Manifest)
           (java.util Properties)))

(defn name-to-keyword [kvs]
  (reduce (fn [m [k v]] (assoc m (keyword (str k)) v)) {} kvs))

(defn java-component-properties
  [comp]
  (let [props (Properties.)]
    (.load props (input-stream comp))
    props))

(defn base [m] (:base m))

(defn component-in-module
  [m sect compn]
  (file-str (base m) "/" sect compn ".properties"))

(defn cleanse
  [s pats]
  (reduce (fn [so-far next] (str/replace so-far next "")) s pats))

(defn component-names-in
  [body]
  (map first (re-seq #"(/[a-zA-Z][\w\d]*)+"
                     (cleanse body [#"#[^\n]*\n" #"http(s)?://[^\n]*\n" #"=/[^\n]*.xml\n"]))))

(defstruct component :section :name :body :path :references)

(defn parse-component
  [m sect compn]
  (let [path (component-in-module m sect compn)
        body (slurp path)]
    (merge (name-to-keyword (java-component-properties path))
           (struct component sect compn body path (component-names-in body)))))

(defn suffix? [f suffix] (.endsWith (.getPath f) suffix))
(defn properties? [f]  (suffix? f ".properties"))
(defn java? [f] (suffix? f ".java"))
(defn jsp? [f] (or (suffix? f ".jsp") (suffix? f ".jspf")))

(defn file-type-seq [fn dir] (filter fn (file-seq dir)))

(defn component-files [m sub] (file-type-seq properties? (file-str (base m) "/" sub)))
(defn source-files [m] (file-type-seq java? (base m)))

(defn make-jsp [name file] (list name file))
(defn jsp-file [j] (second j))
(defn jsp-name [j] (first j))

(defn make-web-module [name base] (list name base))
(defn web-module-name [webm] (first webm))
(defn web-module-base [webm] (second webm))
(defn make-jsp-in-module [webm jspf] (make-jsp (str/replace jspf (str (web-module-base webm)) "") jspf))
(defn web-module-jsps [webm]
  (map #(make-jsp-in-module webm %) (file-type-seq jsp? (web-module-base webm))))

(defn make-web-module-in-module [m webmodn] (make-web-module webmodn (file-str (base m) "/j2ee/" webmodn)))
(defn web-modules [m]
  (if-let [webmods (:ATG-Web-Module m)] 
    (for [modn (str/split webmods #" ")]
      (make-web-module-in-module m (str/replace modn #"^.*/" "")))
    '()
    ))

(defn jsp-files [m] (file-type-seq jsp? (file-str (base m) "/"  )))

(defn- canonicalize
  [m prefix compf]
  (let [compn (.replaceFirst (.getCanonicalPath compf) (str (.getCanonicalPath (base m)) "/" prefix) "")
        compn (.replace compn ".properties" "")]
    compn))

(defn component-names
  ([m]
     (flatten (conj (component-names m "config") (component-names m "liveconfig"))))
  ([m sect]
     (map #(canonicalize m sect %) (component-files m sect))))

(defn manifest-file
  [dir]
  (file-str dir "/META-INF/MANIFEST.MF"))

(defn- java-manifest
  [dir]
  (Manifest. (input-stream (manifest-file dir))))

(defn parse-manifest
  [dir]
  "Locate and parse the manifest file for a module with the given directory and qualified name"
  (assoc
   (name-to-keyword (.. (java-manifest dir) getMainAttributes))
   :qname (str "dgt." (.getName dir))
   :base dir))

(defn has-module-manifest?
  [mdir]
  "Answers whether the directory is an ATG module."
  (.exists (manifest-file mdir)))

(defn module-directories
  [root]
  (filter has-module-manifest? (file-seq (file root))))

(defn load-modules
  [root]
  (map #(parse-manifest %) (module-directories root)))
