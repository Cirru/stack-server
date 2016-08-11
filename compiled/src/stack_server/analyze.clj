
(ns stack-server.analyze
  (:require [clojure.string :as string] [cirru.sepal :as sepal]))

(defn depends-on? [x y dict level]
  (if (contains? dict x)
    (let [deps (get dict x)]
      (if (contains? deps y)
        true
        (if (> level 3)
          false
          (some
            (fn [child] (depends-on? child y dict (inc level)))
            deps))))
    false))

(defn strip-atom [token]
  (if (= (first token) "@") (subs token 1) token))

(defn generate-file [ns-line definitions procedure-line]
  (let [ns-name (get ns-line 1)
        var-names (->>
                    (keys definitions)
                    (map
                      (fn [var-name]
                        (last
                          (string/split var-name (re-pattern "/")))))
                    (into (hash-set)))
        deps-info (->>
                    definitions
                    (map
                      (fn [entry]
                        (let [path (key entry)
                              tree (val entry)
                              var-name (last
                                         (string/split
                                           path
                                           (re-pattern "/")))
                              dep-tokens (->>
                                           (subvec tree 2)
                                           (flatten)
                                           (distinct)
                                           (map strip-atom)
                                           (filter
                                             (fn 
                                               [token]
                                               (contains?
                                                 var-names
                                                 token)))
                                           (into (hash-set)))]
                          [var-name dep-tokens])))
                    (into {}))
        self-deps-names (filter
                          (fn [x] (depends-on? x x deps-info 0))
                          var-names)
        sorted-names (sort
                       (fn [x y]
                         (cond
                           (depends-on? x y deps-info 0) 1
                           (depends-on? y x deps-info 0) -1
                           :else 0))
                       var-names)
        declarations (->>
                       self-deps-names
                       (map (fn [var-name] ["declare" var-name]))
                       (into []))
        definition-lines (map
                           (fn [var-name]
                             (get
                               definitions
                               (str ns-name "/" var-name)))
                           sorted-names)
        tree (into
               []
               (concat
                 [ns-line]
                 declarations
                 definition-lines
                 [procedure-line]))]
    (println "definitions:")
    (println definitions)
    (println (sepal/make-code tree)))
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
        (->>
          namespace-names
          (map
            (fn [ns-name]
              (generate-file
                (get-in collection [:namespaces ns-name])
                (->>
                  (:definitions collection)
                  (filter
                    (fn [entry]
                      (string/starts-with?
                        (key entry)
                        (str ns-name "/"))))
                  (into {}))
                (or (get-in collection [:procedures ns-name]) []))))))
      (do
        (println "Error: spaces not match!")
        (println "from definitions:" namespace-names)
        (println "from namespaces:" namespace-names')
        {}))))
