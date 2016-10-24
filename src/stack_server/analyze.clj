
(ns stack-server.analyze
  (:require [clojure.string :as string] [cirru.sepal :as sepal] [clansi :refer [style]]))

(defn depends-on? [x y dict level]
  (if (contains? dict x)
    (let [deps (:tokens (get dict x))]
      (if (contains? deps y)
        true
        (if (> level 4)
          false
          (some (fn [child] (depends-on? child y dict (inc level))) deps))))
    false))

(def def-names #{"defonce" "def"})

(defn deps-insert [acc new-item items deps-info]
  (if (empty? items)
    (conj acc new-item)
    (let [cursor (first items)]
      (if (depends-on? cursor new-item deps-info 0)
        (if (depends-on? new-item cursor deps-info 0)
          (if (contains? def-names (:kind (get deps-info new-item)))
            (recur (conj acc cursor) new-item (rest items) deps-info)
            (into [] (concat acc [new-item] items)))
          (into [] (concat acc [new-item] items)))
        (recur (conj acc cursor) new-item (rest items) deps-info)))))

(defn ns->path [namespace-name]
  (-> namespace-name
      (string/replace (re-pattern "\\.") "/")
      (string/replace (re-pattern "-") "_")))

(defn strip-atom [token] (if (string/starts-with? token "@") (subs token 1) token))

(defn deps-sort [acc items deps-info]
  (if (empty? items)
    acc
    (let [cursor (first items), next-acc (deps-insert [] cursor acc deps-info)]
      (recur next-acc (into [] (rest items)) deps-info))))

(defn generate-file [ns-line definitions procedure-line]
  (let [ns-name (get ns-line 1)
        var-names (->> (keys definitions)
                       (map (fn [var-name] (last (string/split var-name (re-pattern "/")))))
                       (into (hash-set)))
        deps-info (->> definitions
                       (map
                        (fn [entry]
                          (let [path (key entry)
                                tree (val entry)
                                var-name (last (string/split path (re-pattern "/")))
                                dep-tokens (->> (subvec tree 2)
                                                (flatten)
                                                (distinct)
                                                (map strip-atom)
                                                (filter
                                                 (fn [token]
                                                   (and (contains? var-names token)
                                                        (not= token var-name))))
                                                (into (hash-set)))]
                            [var-name {:kind (first tree), :tokens dep-tokens}])))
                       (into {}))
        self-deps-names (filter (fn [x] (depends-on? x x deps-info 0)) var-names)
        sorted-names (deps-sort [] (into [] var-names) deps-info)
        declarations (->> self-deps-names
                          (map (fn [var-name] ["declare" var-name]))
                          (into []))
        definition-lines (map
                          (fn [var-name] (get definitions (str ns-name "/" var-name)))
                          sorted-names)
        tree (into [] (concat [ns-line] declarations definition-lines procedure-line))
        code (sepal/make-code tree)]
    (comment println "before sort:" var-names)
    (comment println "after  sort:" sorted-names)
    (comment println "generated file:" code)
    code))

(defn collect-files [collection]
  (let [namespace-names (into (hash-set) (keys (:namespaces collection)))
        namespace-names' (into
                          (hash-set)
                          (distinct
                           (map
                            (fn [definition-name]
                              (first (string/split definition-name (re-pattern "/"))))
                            (keys (:definitions collection)))))]
    (if (= namespace-names namespace-names')
      (doall
       (->> namespace-names
            (map
             (fn [ns-name]
               [(ns->path ns-name)
                (generate-file
                 (get-in collection [:namespaces ns-name])
                 (->> (:definitions collection)
                      (filter
                       (fn [entry] (string/starts-with? (key entry) (str ns-name "/"))))
                      (into {}))
                 (or (get-in collection [:procedures ns-name]) []))]))
            (into {})))
      (do
       (println (style "Error: namespaces not match!" :red))
       (println "    from definitions:" (pr-str namespace-names))
       (println "    from namespaces: " (pr-str namespace-names'))
       {}))))
