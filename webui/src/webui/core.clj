(ns webui.core
  (:use [webui nav search module component classes]
        [clojure.java.io]
        [compojure core response]
        [ring.adapter.jetty :only [run-jetty]]
        [ring.util.response]
        [ring.middleware file file-info stacktrace reload])
  (:require [compojure.route :as route]
            view))

(extend-protocol Renderable
  fleet.util.CljString
  (render [this _] (response (.toString this))))

(defn index-page
  []
  {:breadcrumbs (home-crumbs)
   :body (view/index)})

(defn layout-or-404
  [body]
  (if body (view/layout body) nil))

(defroutes main-routes
  (GET "/" [] (view/layout (index-page)))
  (GET ["/modules/:qname" :qname #".*"] [qname] (layout-or-404 (module-page qname)))
  (GET "/modules" [] (layout-or-404 (modules-page)))
  (GET "/components" [] (layout-or-404 (components-page)))
  (GET "/component/*" {{compn "*"} :route-params} (layout-or-404 (component-page compn)))
  (GET "/classes" [] (view/layout {:breadcrumbs (classes-crumbs) :body (classes-page)}))
  (GET "/v1/modules" [] (modules-api))
  (GET "/v1/components" [] (components-api))
  (GET ["/v1/components/module/:qname" :qname #".*"] [qname] (components-in-module qname))
  #_(GET "/v1/classes" [] (classes-api))
  (GET "/v1/jsps" [] "<p>Coming soon...</p>")
  (route/files "/")
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
