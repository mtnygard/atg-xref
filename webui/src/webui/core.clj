(ns webui.core
  (:use webui.nav
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

(defn index-page [] (view/index))

(defn modules-page [] (view/modules))

(defroutes main-routes
  (GET "/" [] (view/layout {:breadcrumbs (home-crumbs) :body (index-page)}))
  (GET "/modules" [] (view/layout {:breadcrumbs (modules-crumbs) :body (modules-page)}))
  (route/not-found (file "public/404.html")))

(def app (-> main-routes
             (wrap-reload '(webui.core))
             (wrap-reload '(view))
             (wrap-reload '(helpers))
             (wrap-file "public")
             (wrap-file-info)
             (wrap-stacktrace)))

(defn start-server
  []
  (def *server* (agent (run-jetty #'app {:port 8080}))))

(defn -main [& args]
  (start-server))
