(ns indexer.java
  (:use [clojure.java.io :only (file reader)])
  (:require [clojure.contrib.str-utils2 :as str])

  (:import [com.habelitz.jsobjectizer.unmarshaller JSourceUnmarshaller]
           [com.habelitz.jsobjectizer.jsom.util TraverseAction TraverseActionAdapter]
           [com.habelitz.jsobjectizer.jsom.api JavaSource ClassDeclaration]))

(def testfile (file "test/resources/AcidTest.java"))

(def realfile (file "/Users/mtnygard/work/bestbuy/applications/dgt_mtnygard_build/dgt/modules/commerce/src/java/com/bestbuy/digiterra/commerce/inventory/droplet/InventoryMessageDroplet.java"))

(def teststring "package foo.bar; public class Hello { public static void main(String[] args) { System.out.println(\"Hello, world!\"); } }")

(def teststring2 "import java.util.List; import static org.hamcrest.Matchers.*; public class Hello { public static void main(String[] args) { System.out.println(\"Hello, world!\"); } }")


(defn parse [f] (.unmarshal (JSourceUnmarshaller.) f nil))

(defn package-decl [tree] (.toString (.getPackageDeclaration tree)))

(defn- drop-last-segment [qualifiedname]
  (str/join "." (reverse (drop 1 (reverse (str/split qualifiedname #"\."))))))

(defn import-class [decl]
  (let [static (.isStaticImport decl)
        multi (.isMultiImport decl)
        qname (.toString (.getImportPath decl))]
    (cond
     (and static multi) qname
     static (drop-last-segment qname)
     :else qname)))

(defn import-decls [tree]
  (set (map import-class (iterator-seq (.getImportDeclarations tree)))))

(defprotocol TypeContainer
  "Language elements that can contain type declarations"
  (declared-types [container qualifier] "Get a lazy sequence of the types declared by this container"))

(extend-protocol TypeContainer
  ClassDeclaration
  (declared-types [c q]
                  (if-let [inner-decls (.getInnerTypeDeclarations (.getTopLevelScope c))]
                    (for [inner (iterator-seq inner-decls)]
                      (str q "$" (.getIdentifier inner)))))

  JavaSource
  (declared-types [c q]
                  (flatten (for [top (iterator-seq (.getTypeDeclarations c))]
                             (let [qname (str q "." (.getIdentifier top))]
                               (cons qname (declared-types top qname)) )))))
   

(defn type-decls [tree]
  (let [packagename (package-decl tree)]
    (map #(str packagename "." (.getIdentifier %)) (iterator-seq (.getTypeDeclarations tree)))
    )
  )

(defn type-refs [tree]
  )
