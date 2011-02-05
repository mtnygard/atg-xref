(defproject atg-xref-indexer "1.0.0-SNAPSHOT"
  :description "Indexer for ATG codebase cross-reference."
  :dependencies [[org.clojure/clojure "1.2.0"]
                 [org.clojure/clojure-contrib "1.2.0"]
                 [org.clojars.kjw/solrj "1.4.0"]
                 [org.antlr/antlr "3.2"]
                 ]
  :dev-dependencies [[swank-clojure "1.3.0-SNAPSHOT"]
                     [lein-antlr "0.1.0"]
                     [lein-javac "1.2.1-SNAPSHOT"]]
  :source-path "src/clojure"
  :java-source-path "src/gen-java"
  :main indexer.core
  :antlr-src-dir "src/antlr"
  :antlr-dest-dir "src/gen-java"
  :hooks [leiningen.antlr])
