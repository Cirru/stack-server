
(ns stack-server.analyze
  (:require [clojure.string :as string]))

(defn generate-file [informations]
  (println "informations" informations)
  "file....")

(defn collect-files [collection]
  (let [namespace-names (keys (:namespaces collection))
        file-names (map
                     (fn [namespace-name]
                       (-> namespace-name
                        (string/replace (re-pattern "\\.") "/")
                        (string/replace (re-pattern "-") "_")))
                     namespace-names)
        namespace-names' (distinct
                           (map
                             (fn [definition-name]
                               (first
                                 (string/split
                                   definition-name
                                   (re-pattern "/"))))
                             (keys (:definitions collection))))]
    (if (= (sort namespace-names) (sort namespace-names'))
      (doall
        (map
          (fn [ns-name]
            (println "loop ns-name")
            (generate-file
              {:definitions
               (into
                 {}
                 (filter
                   (fn [entry]
                     (string/starts-with?
                       (first entry)
                       (str ns-name "/")))
                   (:definitions collection))),
               :procedure
               (or (get-in collection [:procedures ns-name]) []),
               :namespace (get-in collection [:namespaces ns-name])}))
          namespace-names))
      (do
        (println "Error: spaces not match!")
        (println "from definitions:" namespace-names)
        (println "from namespaces:" namespace-names')
        {}))))
