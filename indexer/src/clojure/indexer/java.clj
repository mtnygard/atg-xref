(ns indexer.java
  (:use [clojure.java.io :only (file reader)])
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

