(ns webui.nav)

(defn crumb [l t] {:link l :title t})
(defn link-to [m] {:link (str "/modules/" (.get m "name")) :name (.get m "name")})

(defn home-crumbs [] (list (crumb "/" "Home")))
(defn components-crumbs [] (conj (home-crumbs) (crumb "/components" "Components")))
(defn classes-crumbs [] (conj (home-crumbs) (crumb "/classes" "Classes")))
