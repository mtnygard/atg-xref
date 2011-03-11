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

(defn parse [f]
  (let [tokens (CommonTokenStream. (JavaLexer. (antlr-stream f)))
        parser (JavaParser. tokens)]
    (.getTree (.compilationUnit parser))))

(defn token-type [tn] (if (nil? (.getToken tn)) nil (.getType (.getToken tn))))

(def lexical-scope-tokens [JavaParser/PACKAGE JavaParser/TYPEDECL])

(defn qualifiers
  [tn qs]
  (let [newq (fn [tn] (.getText (.get (.getChildren tn) 0)))]
    (conj qs (some #(if (= (token-type tn) %) (newq tn)) lexical-scope-tokens) )))

(defn mktree
  ([tn] (mktree tn []))
  ([tn qs] (list
            (token-type tn)
            (.getText tn)
            (map #(mktree % (qualifiers tn qs)) (seq (.getChildren tn)))
            (apply str (interpose "." qs)))))

(defn- child-nodes [n] (nth n 2))
(defn- branch? [n] (not (empty? (child-nodes n))))
(defn- text-of-child-node [node n] (second (nth (child-nodes node) n)))
(defn parse-seq [f] (tree-seq branch? child-nodes (mktree (parse f))))

(defmacro defnode
  [nm tt]
  `(defn ~nm [n#] (= ~tt (first n#))))

(defnode package-node JavaParser/PACKAGE)
(defnode import-node JavaParser/IMPORT)
(defnode typedecl-node JavaParser/TYPEDECL)
(defnode typeref-node JavaParser/TYPEREF)

(defn package-decl [ast]
  (when-let [pkgtokens (filter package-node ast)]
    (text-of-child-node (first pkgtokens) 0)))

(defn import-decls
  [ast]
  (for [node (filter import-node ast)]
    (let [args (map second (child-nodes node))]
      (cons (first args) (map keyword (rest args))))))

(defn type-decls
  [ast]
  (map #(nth (first (child-nodes %)) 3) (filter typedecl-node ast)))

(defn type-refs
  [ast]
  (map #(nth (first (child-nodes %)) 1) (filter typeref-node ast)))

(defn summary-info
  [f]
  (let [ast (parse-seq f)]
    {:package (package-decl ast)
     :references (conj (import-decls ast) (type-refs ast))

     }))
