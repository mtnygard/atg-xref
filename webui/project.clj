(defproject atg-xref-webui "1.0.0-SNAPSHOT"
  :description "Human interface for ATG codebase cross-reference."
  :dependencies [[org.clojure/clojure "1.2.0"]
                 [org.clojure/clojure-contrib "1.2.0"]
                 [org.clojars.kjw/solrj "1.4.0"]]
  :dev-dependencies [[swank-clojure "1.2.1"]]
  :main webui.core)
