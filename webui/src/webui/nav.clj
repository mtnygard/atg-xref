(ns webui.nav)


(defn crumb [l t] {:link l :title t})

(defn home-crumbs [] (list (crumb "/" "Home")))
(defn modules-crumbs [] (conj (home-crumbs) (crumb "/modules" "Modules")))
(defn module-crumbs [mod] (conj (modules-crumbs) (crumb mod mod)))

