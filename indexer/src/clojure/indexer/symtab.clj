(ns indexer.symtab
  "Symbol table for Java sources"
  (:require [clojure.zip :as zip]))

(defn topmost
  "Like clojure.zip/root, but returns the loc rather than the node."
  [loc]
  (if (= :end (loc 1))
    loc
    (let [p (zip/up loc)]
      (if p
        (recur p)
        loc))))

(defn find-child
  [loc n]
  (loop [cld (zip/down loc)]
    (if (nil? cld)
      nil
      (if (= (cld 0) n)
        cld
        (recur (zip/right cld))))))

(def *global-symtab* (-> (zip/vector-zip '[]) (zip/append-child ".") ref))

(defmacro edit-tree
  "Define a new tree-editing function."
  [body]
  `(dosync (alter *global-symtab* (~@body))))

(defn declare-package
  "Begin a new package. Moves to root of symtab, leaves loc pointing at the package node itself"
  [name]
  (edit-tree
   (fn [loc]
     (let [top (topmost loc)
           existing (find-child top name)]
       (if existing
         existing
         (-> top (zip/append-child name)))))))

(defn declare-class
  [name & opts]
  (let [node (reduce (fn [m [k v]] (assoc m k v)) {:name name} (partition 2 opts))]
    (edit-tree
     (fn [loc] (zip/append-child loc node)))))
