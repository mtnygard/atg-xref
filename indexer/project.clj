(defproject atg-xref-indexer
  :version :git
  :description "Indexer for ATG codebase cross-reference."
  :dependencies [[org.clojure/clojure "1.2.0"]
                 [org.clojure/clojure-contrib "1.2.0"]
                 [org.clojars.kjw/solrj "1.4.0"]
                 [org.antlr/antlr "3.2"]]
  :dev-dependencies [[swank-clojure "1.3.0-SNAPSHOT"]]
  :main indexer.core
)
