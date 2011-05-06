(ns indexer.jsp
  "Simplistic parser for JSP files. Does not validate syntax, may get extremely confused at times."
  (:use atg.module
        [clojure.set :only (union)])
  (:require [clojure.string :as str]))

(defn- re-extract
  [re body]
  (set (map second (re-seq re body))))

(defn page-imports
  [body]
  (re-extract #"<%@\s*page\s+import\s*=\s*\"(.*)(.*)?\"" body))

(defn start-code-tag
  [body]
  (re-extract #"<!--\s*(B:...)\s*-->" body))

(defn end-code-tag
  [body]
  (re-extract #"<!--\s*(E:...)\s*-->" body))

(defn taglibs
  [body]
  (re-extract #"<%@\s*taglib\s+uri=\"([^\"]*)\"" body))

(defn bean-references
  [body]
  (union
   (re-extract #"dsp:importbean\s+bean=\"(.*)\"" body)
   (re-extract #"dsp:droplet\s+name=\"(/[^\"]*)\"" body)))

(defstruct jsp-info :name :body :page-imports :start-code-tag :end-code-tag :taglibs :references)

(defn parse-jsp
  ([name body]
     (struct jsp-info name body (page-imports body) (start-code-tag body) (end-code-tag body) (taglibs body) (bean-references body)))
  ([module web-module jsp]
     (parse-jsp (jsp-name jsp) (slurp (jsp-file jsp)))))
