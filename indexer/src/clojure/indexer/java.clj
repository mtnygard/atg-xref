(ns indexer.java
  (:import [org.antlr.runtime ANTLRFileStream CommonTokenStream]
           JavaParser
           JavaLexer))

(defn parse-file [f]
  (let [lexer (JavaLexer. (ANTLRFileStream. f))
        tokens (CommonTokenStream. lexer)
        parser (JavaParser. tokens)]
    (.getTree (.compilationUnit parser))))

(defn AST [node]
  (if (zero? (.getChildCount node))
    (.getText node)
    (let [children (map AST (.getChildren node))
          txt (.getText node)]
      (if txt
        (cons txt children)
        children))))
