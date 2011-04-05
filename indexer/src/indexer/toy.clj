(ns indexer.toy)

(def original {:tag 1
               :identifier "top"
               :subvals '(
                         {:tag 2 :identifier "ignoreme" :subvals ({:tag 5 :identifier "keeper-in-a-reject"})}
                         {:tag 1 :identifier "keepme" :subvals ({:tag 3 :identifier "nestedkeeper"})}
                         )
               :othervals '(
                            {:tag 1 :identifier "keeper" :scopes ({:tag 2 :identifier "ignored"})}
                            {:tag 6 :identifier "drop" :scopes ({:tag 8 :identifier "bye-bye"}
                                                                {:tag 10 :identifier "not-sticking"})}
                           )
               })

(defn interesting-of [c mappr]
  (let [interesting-entry (fn [m e]
                            (let [k (key e) v (val e)]
                              (if (coll? v)
                                (assoc m k (interesting-of v mappr))
                                (assoc m k v)
                                )))
        has-colls? #(reduce (fn [t v] (or t (coll? v))) false (vals %))
        vacant-coll? #(not (and (coll? %) (empty? %)))]
    (cond
     (seq? c) (let [reduced (filter vacant-coll? (map #(interesting-of % mappr) c))]
                (if (empty? reduced) nil reduced))
     (map? c) (let [reduced (reduce interesting-entry {} (seq c))]
                (if (or (has-colls? reduced) (mappr reduced))
                  reduced
                  {}))
     :else c
     )
))

(declare interesting2)

(defn compound-val? [[k v]] (coll? v))

(defn simple-part [m]
  (filter (comp not compound-val?) (seq m)))

(defn compound-part [m]
  (filter compound-val? (seq m)))

(defn filter-compound [mappr [k v]]
  {k (for [e v]
       (cond
        (map? e) (interesting2 e mappr)
        (seq? e) (map #(interesting2 % mappr) e)
        :else e))})

(defn interesting2 [m mappr]
  (let [merge-entry (fn [m [k e]] (assoc m k e))
        partials (conj (simple-part m) (map #(filter-compound mappr %) (compound-part m)))
        merged (reduce merge-entry {} partials)]
    (if (mappr merged) merged nil)
))
