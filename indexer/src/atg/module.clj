(ns atg.module
  (:use [clojure.contrib.io :only (file-str)])
  (:use atg.manifest))

(defn subdirs
  [d]
  (filter #(.isDirectory %) (.listFiles d)))

(defn all-possible-module-directories
  [root]
  (flatten (map #(subdirs (file-str root "/" %)) '("apps" "modules" "zones"))))

(defn module-directories
  [root]
  (filter is-module? (all-possible-module-directories root)))

(defn load-modules
  [root]
  (map #(parse-manifest root %) (module-directories root)))
