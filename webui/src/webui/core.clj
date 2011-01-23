(ns webui.core
  (:use webui.nav
        webui.search
        [clojure.java.io]
        [compojure core response]
        [ring.adapter.jetty :only [run-jetty]]
        [ring.util.response]
        [ring.middleware file file-info stacktrace reload])
  (:require [compojure.route :as route]
            view)
  (:gen-class))

(extend-protocol Renderable
  fleet.util.CljString
  (render [this _] (response (.toString this))))

(defn link-to [m] {:link (str "/modules/" (.get m "name")) :name (.get m "name")})

(defn index-page [] (view/index {:modules (map link-to (modules-matching "*"))}))

(defn modules-page [] (view/modules (map link-to (modules-matching "*"))))

(defn components-page [] (view/components))

(defn classes-page [] (view/classes))

(defroutes main-routes
  (GET "/" [] (view/layout {:breadcrumbs (home-crumbs) :body (index-page)}))
  (GET "/modules" [] (view/layout {:breadcrumbs (modules-crumbs) :body (modules-page)}))
  (GET "/components" [] (view/layout {:breadcrumbs (components-crumbs) :body (components-page)}))
  (GET "/classes" [] (view/layout {:breadcrumbs (classes-crumbs) :body (classes-page)}))
  (route/not-found (file "public/404.html")))

(defn app-routes
  []
  (-> main-routes
      (wrap-solr "http://localhost:8983/solr")
      (wrap-reload '(webui.core view helpers webui.nav))
      (wrap-file "public")
      (wrap-file-info)
      (wrap-stacktrace)))

(defn start-server
  []
  (run-jetty (app-routes) {:port 8080 :join? false}))

(defn -main [& args]
  (start-server))
