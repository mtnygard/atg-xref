(ns webui.nav)

(defn crumb [l t] {:link l :title t})

(defn home-crumbs [] (list (crumb "/" "Home")))
