(ns helpers
  (:use [clojure.contrib.str-utils :only (str-join)]))

(defn this-year [] (.get (java.util.Calendar/getInstance) java.util.Calendar/YEAR))


