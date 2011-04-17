(ns indexer.symtab
  "Symbol table for Java sources"
  (:use (clojure.contrib condition)))

(def empty-environment '())

(defn first-frame
  "Get the innermost enclosing scope"
  [env] (first env))

(defn enclosing-environment
  "Get the environment above the current one"
  [env]
  (rest env))

(defn make-frame
  "Define a new frame"
  [vars types]
  (zipmap vars types))

(defn frame-variables
  "Get the list of variables defined in this scope frame"
  [frame]
  (keys frame))

(defn frame-types
  "Get the list of types for the variables defined in this scope frame"
  [frame]
  (second frame))

(defn frame-variable-type
  [frame var]
  "Get the type of a variable in a frame"
  (frame var))

(defn extend-environment
  "Add a new scope frame in the environment"
  [env frame]
  (cons frame env))

(defn add-binding-to-frame
  "Return a new frame with the additional variable binding"
  [frame var type]
  (assoc frame var type))

(defn add-binding-to-current-scope
  "Return a new environment with the additional binding in the current scope"
  [env var type]
  (extend-environment (enclosing-environment env)
                      (add-binding-to-frame (first-frame env) var type)))

(defn lookup-variable-type
  "Look up the type of a variable, anywhere in the list of enclosing environments"
  [env var]
  (if (= env empty-environment)
    (raise :message "Unbound variable")
    (if-let [result (frame-variable-type (first-frame env) var)]
      result
      (recur (enclosing-environment env) var))))
