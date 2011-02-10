(ns indexer.java
  (:use [clojure.java.io :only (file)])
  (:require [clojure.zip :as z]
            [clojure.contrib.zip-filter :as zf])
  (:import [org.antlr.runtime ANTLRFileStream ANTLRStringStream CommonTokenStream]
           [org.antlr.runtime.tree CommonTree]
           [indexer.java JavaParser JavaLexer]))

(def testfile (file "test/resources/AcidTest.java"))

(def realfile (file "/Users/mtnygard/work/bestbuy/applications/dgt_mtnygard_build/dgt/modules/commerce/src/java/com/bestbuy/digiterra/commerce/inventory/droplet/InventoryMessageDroplet.java"))

(def teststring "package foo.bar; public class Hello { public static void main(String[] args) { System.out.println(\"Hello, world!\"); } }")

(def teststring2 "import java.util.List; import static org.hamcrest.Matchers.*; public class Hello { public static void main(String[] args) { System.out.println(\"Hello, world!\"); } }")

(defprotocol TokenStreamSource
  (antlr-stream [this]))

(extend-type String
  TokenStreamSource
  (antlr-stream [s] (ANTLRStringStream. s)))

(extend-type java.io.File
  TokenStreamSource
  (antlr-stream [f] (ANTLRFileStream. (.getPath f))))

(defn token-stream [f] (CommonTokenStream. (JavaLexer. (antlr-stream f))))

(defn parse [f]
  (let [tokens (token-stream f)
        parser (JavaParser. tokens)]
    (.getTree (.compilationUnit parser))))

(defn ast-branch? [tn]  (not (zero? (.getChildCount tn))))
(defn ast-children
  [n]
  (if (coll? n)
    (flatten (map ast-children n))
    (seq (.getChildren n))))

(defn parse-seq [f] (tree-seq ast-branch? ast-children (parse f)))

(defn ast-nodes
  "Predicate that matches AST tree nodes with the token type tkn.
   You'll usually want to make a partial for the tokens you actually want to extract"
  [tkn node] (and (.getToken node) (= tkn (.getType (.getToken node)))))

;;; TODO: Make a macro to build these partials for me.
(def package-node (partial ast-nodes JavaParser/PACKAGE))
(def import-node (partial ast-nodes JavaParser/IMPORT))
(def typedecl-node (partial ast-nodes JavaParser/TYPEDECL))

(defn package-decl
  [ast]
  (when-let [pkg (first (ast-children (filter package-node ast)))]
    (.getText pkg)))

(defn import-decls
  [ast]
  (for [node (filter import-node ast)]
    (let [args (map #(.getText %) (ast-children node))]
      (cons (first args) (map keyword (rest args))))))

(defn type-decls
  [ast]
  (let [package (package-decl ast)]
    (map #(str package "." (.getText (first (.getChildren %)))) (filter typedecl-node ast))))
