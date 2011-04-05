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

(defn mk-empty-symtab [] (-> (zip/vector-zip '[["."]])))

(def *global-symtab* (ref (mk-empty-symtab)))

(defn reset-symtab [] (dosync (ref-set *global-symtab* (mk-empty-symtab))))

(defmacro edit-tree
  "Define a new tree-editing function."
  [body]
  `(dosync (alter *global-symtab* (~@body))))

(defn append-and-navigate [loc child]
  (->
   (zip/append-child loc [child])
   zip/down
   zip/rightmost))

(defn declare-package
  "Begin a new package. Moves to root of symtab, leaves loc pointing at the package node itself"
  [name]
  (prn "Declaring package " name)
  (dosync (alter *global-symtab* 
                 (fn [loc]
                   (let [top (topmost loc)
                         existing (find-child top name)]
                     (if existing
                       existing
                       (append-and-navigate top name)))))))

(defn declare-class
  [name & opts]
  (prn "Declaring class " name)
  (let [node (reduce (fn [m [k v]] (assoc m k v)) {:name name} (partition 2 opts))]
    (dosync (alter *global-symtab* append-and-navigate node))))

(defn exit-scope []
  (dosync (alter *global-symtab* zip/up)))

(defmacro with-class
  [name & body]
  `(do
     (declare-class ~name)
     ~@body
     (exit-scope)))
