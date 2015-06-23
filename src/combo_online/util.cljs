(ns combo-online.util)

(defn remove-from-vector [v pos]
  (vec (concat (subvec v 0 pos) (subvec v (inc pos) (count v)))))
