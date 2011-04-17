(ns indexer.java
  (:use [clojure.java.io :only (file reader)]
        [clojure.string :only (split)]
        indexer.symtab)
  (:require [clojure.contrib.str-utils2 :as str])

  (:import [com.habelitz.jsobjectizer.unmarshaller JSourceUnmarshaller]
           [com.habelitz.jsobjectizer.jsom.util JSOMIterator]
           [com.habelitz.jsobjectizer.jsom.api JavaSource ClassDeclaration QualifiedIdentifier ClassTopLevelScope EnumTopLevelScope]
           [com.habelitz.jsobjectizer.jsom JSOM]))

(def testfile (file "test/resources/AcidTest.java"))

(def realfile (file "/Users/mtnygard/work/bestbuy/applications/dgt_mtnygard_build/dgt/modules/commerce/src/java/com/bestbuy/digiterra/commerce/inventory/droplet/InventoryMessageDroplet.java"))

(def teststring "package foo.bar; public class Hello { public static void main(String[] args) { System.out.println(\"Hello, world!\"); } }")

(def teststring2 "import java.util.List; import static org.hamcrest.Matchers.*; public class Hello { public static void main(String[] args) { System.out.println(\"Hello, world!\"); } }")

(defn parse [f] (.unmarshal (JSourceUnmarshaller.) f nil))

(declare jsom->map)

(defn jsom->bean  "Create a basic map of properties from a JSOM API instance."
  [jsom & exclusions]
  (let [basic-bean (bean jsom)
        bad-keys (into [] exclusions)
        filtered-bean (apply dissoc basic-bean bad-keys)]
    (reduce (fn [m [k v]]
              (assoc m k (if (nil? v) nil (jsom->map v))))
            {} filtered-bean)))

(defprotocol Mappable
  (jsom->map [jsom] "Perform type-specific property conversions."))

(extend-protocol Mappable
  ClassTopLevelScope  (jsom->map [jsom] (jsom->bean jsom :except :ownerClassDeclaration))
  EnumTopLevelScope   (jsom->map [jsom] (jsom->bean jsom :except :owner))
  QualifiedIdentifier (jsom->map [jsom] (.toString jsom))
  JSOMIterator  (jsom->map [jsom] (map jsom->map (iterator-seq jsom)))
  JSOM   (jsom->map [jsom] (jsom->bean jsom))
  Object  (jsom->map [obj] obj))

;;; file -> jsom -> maps

;;; analyze the compilation unit by
;;;  - find the package decl
;;;  - find the imports
;;;  - find the type decls
;;;  - return seq of the declared types as (name package source line)

(defn- last-part-of
  "Split a dotted classname, return the last part"
  [path]
  (last (split path #"\.")))

(defn declare-package
  "Create a frame in the symbol table for the package"
  [env pkg]
  (extend-environment env (make-frame '(%package %current) [pkg pkg])))

(defn declare-precise-import
  "Create a frame representing a precise (i.e., class) import. Not for static imports."
  [env qname]
  (let [pname (last-part-of qname)]
    (add-binding-to-current-scope env pname qname)))

;; (defn process-types [ts]
;;   (doseq [t ts]
;;     (declare-class (:identifier t))
;;     (process-types (get-in t [:topLevelScope :innerTypeDeclarations]))))

(defn process-imports
  "Examine the collection of import statements. Resolve precise imports, leaving wildcards for later"
  [env imports]
  (let [proc-imp (fn [env import]
                   (let [qname (:importPath import)
                         multi (:multiImport import)
                         static (:staticImport import)
                         pname (last-part-of qname)]
                     (cond
                      (and multi static) (add-binding-to-current-scope env qname :static-wildcard)
                      static (add-binding-to-current-scope env qname :static)
                      multi (add-binding-to-current-scope env qname :wildcard)
                      :else (add-binding-to-current-scope env pname qname)))
                   )]
    (reduce proc-imp env imports)))




(defn process-type-decls
  "Walk the list of type declarations, add them below the current scope."
  [env types]
  (defn process-type-decl
    "Record a type declaration from the AST into the symbol table. Returns a new frame."
    [env type]
    (println (:identifier type))
    (add-binding-to-current-scope env (:identifier type) (:identifier type))
    (process-type-decls (extend-environment env (make-frame '() '())) (:innerTypeDeclarations type)))
  (reduce process-type-decl env types))

(defn process-compilation-unit [ast]
  (-> empty-environment
      (declare-package (:packageDeclaration ast))
      (process-imports (:importDeclarations ast))
      (process-type-decls (:typeDeclarations ast))
      ))

(defn jsom-seq [m] (tree-seq map? #(flatten (filter coll? (vals %))) m))

(def class-or-enum #{com.habelitz.jsobjectizer.jsom.JSOM$JSOMType/CLASS_DECLARATION com.habelitz.jsobjectizer.jsom.JSOM$JSOMType/ENUM_DECLARATION})

#_(filter #(some #{(:JSOMType %)} class-or-enum) ast-seq)



#_(defn classes-from [f] (extract-summary f (jsom->map (parse f))))
