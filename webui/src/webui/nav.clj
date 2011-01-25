(ns webui.nav)

(defn crumb [l t] {:link l :title t})

(defn home-crumbs [] (list (crumb "/" "Home")))
(defn classes-crumbs [] (conj (home-crumbs) (crumb "/classes" "Classes")))
