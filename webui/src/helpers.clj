(ns helpers
  (:use [clojure.contrib.str-utils :only (str-join)]))

(defn this-year [] (.get (java.util.Calendar/getInstance) java.util.Calendar/YEAR))

(def #^{:private true}
     escape-xml-map
     (zipmap "'<>\"&" (map #(str \& % \;) '[apos lt gt quot amp])))

(defn escape-xml [text]
  (apply str (map #(escape-xml-map % %) (.toString text))))

