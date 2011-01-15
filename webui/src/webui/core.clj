(ns webui.core
  (:use compojure.core
        ring.adapter.jetty
        compojure.response
        [ring.util.response :only (response content-type)]
        fleet)
  (:require [compojure.route :as route])
  (:gen-class))

(fleet-ns views "templates")

(extend-protocol Renderable
  fleet.util.CljString
  (render [this _] (response (.toString this))))

(defroutes main-routes
  (GET "/" [] (views/index))
  (route/not-found "<h1>Page not found</h1>"))

(defn -main [& args]
  (run-jetty main-routes {:port 8080}))
