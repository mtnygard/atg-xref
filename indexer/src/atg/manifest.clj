(ns atg.manifest
  (:use [clojure.contrib.io :only (read-lines file-str)])
  (:use [clojure.contrib.string :only (blank? split)])
  (:import (java.io FileInputStream))
  (:import (java.util.jar Manifest)))

(defn manifest-file
  [dir]
  (file-str dir "/META-INF/MANIFEST.MF"))

(defn- java-manifest
  [dir]
  (Manifest. (FileInputStream. (manifest-file dir))))

(defn name-to-string [kvs]
  (reduce (fn [m [k v]] (assoc m (str k) v)) {} kvs))

(defn parse-manifest
  [base dir]
  "Locate and parse the manifest file for a module with the given directory and qualified name"
  (assoc
   (name-to-string (.. (java-manifest dir) getMainAttributes))
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

