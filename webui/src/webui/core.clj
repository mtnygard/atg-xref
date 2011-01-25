(ns webui.core
  (:use [webui nav search module component]
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

(defn index-page [] (view/index {:modules (links-to-all-modules)}))

(defn classes-page [] (view/classes))

(defroutes main-routes
  (GET "/" [] (view/layout {:breadcrumbs (home-crumbs) :body (index-page)}))
  (GET ["/modules/:qname" :qname #".*"] [qname] (view/layout {:breadcrumbs (module-crumbs qname) :body (module-page qname)}))
  (GET "/modules" [] (view/layout {:breadcrumbs (modules-crumbs) :body (modules-page)}))
  (GET "/components" [] (view/layout {:breadcrumbs (components-crumbs) :body (components-page)}))
  (GET "/component/*" {{compn "*"} :route-params} (view/layout {:breadcrumbs (component-crumbs compn) :body (component-page compn)}))
  (GET "/classes" [] (view/layout {:breadcrumbs (classes-crumbs) :body (classes-page)}))
  (route/files "public")
  (route/not-found (file "public/404.html")))

(def app-routes
  (-> main-routes
      (wrap-solr "http://localhost:8983/solr")
      (wrap-reload '(webui.core webui.module webui.component view helpers webui.nav))
      (wrap-file-info)
      (wrap-stacktrace)))

(defn start-server
  [& options]
  (let [options (merge {:port 8080 :join? false} options)]
    (run-jetty (var app-routes) options)))

(defn -main [& args]
  (start-server))
