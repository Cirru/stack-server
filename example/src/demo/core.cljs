
(ns demo.core )

(defn main []
  (let [a 1, b 2, c 3] (println #{1 2 3}) (println #(println %1)) (comment pl) (loop [a 1] ))
  (println #{1 2 3})
  (println #{1 2 3}))
