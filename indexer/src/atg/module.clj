(ns atg.module
  (:use [clojure.contrib.io :only (file-str input-stream)])
  (:import (java.io FileInputStream)
           (java.util.jar Manifest)
           (java.util Properties)))

(defn files-with-extension [^File dir extension]
  (filter #(.endsWith (.getPath %) extension) (file-seq dir)))

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

(defn parse-component
  [m sect compn]
  (let [path (component-in-module m sect compn)]
    (assoc
        (name-to-keyword (java-component-properties path))
      :section sect
      :path path
      :name compn)))

(defn component-files
  ([m sub]
     (files-with-extension (file-str (:base m) "/" sub) ".properties")))

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

(defn subdirs
  [d]
  (filter #(.isDirectory %) (.listFiles d)))

(defn all-possible-module-directories
  [root]
  (flatten (map #(subdirs (file-str root "/" %)) ["apps" "modules" "zones"])))

(defn module-directories
  [root]
  (filter has-module-manifest? (all-possible-module-directories root)))

(defn load-modules
  [root]
  (map #(parse-manifest root %) (module-directories root)))

