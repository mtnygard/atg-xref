(defproject atg-xref-indexer "1.0.0-SNAPSHOT"
  :description "Search and cross-reference for ATG codebase."
  :dependencies [[org.clojure/clojure "1.2.0"]
                 [org.clojure/clojure-contrib "1.2.0"]
                 [org.clojars.kjw/solrj "1.4.0"]]
  :dev-dependencies [[swank-clojure "1.2.1"]]
  :main atg-xref.core)
