(ns atg.manifest
  (:use [clojure.contrib.io :only (read-lines file-str)])
  (:use [clojure.contrib.string :only (blank? split)])
  (:import (java.io FileInputStream))
  (:import (java.util.jar Manifest)))

(defn with-metadata
  [mf dir]
  (assoc mf
    :qname (str "dgt." (.getName dir))
    :base dir))

(defn read-non-blank-lines
  [name]
  (filter (complement blank?) (read-lines name)))

(defn manifest-file
  [dir]
  (file-str dir "/META-INF/MANIFEST.MF"))

(defn- java-manifest
  [dir]
  (Manifest. (FileInputStream. (manifest-file dir))))

(defn name-to-string [kvs]
  (reduce (fn [coll [k v]] (into coll {(.toString k) v})) {} kvs))

(defn parse-manifest
  [base dir]
  "Locate and parse the manifest file for a module with the given directory and qualified name"
  (with-metadata
    (name-to-string (.. (java-manifest dir) getMainAttributes))
    dir))

(defn is-module?
  [mdir]
  "Answers whether the directory could be an ATG module."
  (.exists (manifest-file mdir)))

(defn module-name [mf]
  (get mf :qname ""))

(defn product [mf]
  (get mf "ATG-Product" ""))

(defn dependency-list [mf]
  (re-seq #"\S+" (get mf "ATG-Required" "")))

