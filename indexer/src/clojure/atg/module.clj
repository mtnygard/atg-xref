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

(defn component-in-module
  [m sect compn]
  (file-str (:base m) "/" sect compn ".properties"))

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

(defn suffix-p [suffix f] (.endsWith (.getPath f) suffix))
(def properties? (partial suffix-p ".properties"))
(def java? (partial suffix-p ".java"))

(defn component-files [m sub] (filter properties? (file-seq (file-str (:base m) "/" sub))))
(defn source-files [m] (filter java? (file-seq (:base m))))

(defn- canonicalize
  [m prefix compf]
  (let [compn (.replaceFirst (.getCanonicalPath compf) (str (.getCanonicalPath (:base m)) "/" prefix) "")
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
  [base dir]
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
  (map #(parse-manifest root %) (module-directories root)))
