(ns webui.core
  (:use compojure.core
        compojure.response
        [ring.adapter.jetty :only [run-jetty]]
        [ring.util.response :only [response content-type]]
        [ring.middleware.file :only [wrap-file]]
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

(def app (-> main-routes
             (wrap-file "public")))

(defn -main [& args]
  (run-jetty (var app) {:port 8080}))
