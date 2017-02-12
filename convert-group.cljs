
(ns convert.core
  (:require [cljs.reader :refer [read-string]]
            [clojure.string :as string]
            [fipp.clojure :refer [pprint]]))

(def fs (js/require "fs"))

(def ir-path "stack-sepal.ir")

(defn get-by-ns [entry]
  (let [the-key (first entry)]
    (first (string/split the-key "/"))))

(defn only-name [piece]
  (last (string/split piece "/")))

(defn get-entry [pair]
  (let [[the-name the-tree] pair
        branch (->> the-tree
                    (map (fn [entry]
                            (let [[k v] entry]
                              [(only-name k) v])))
                    (into {}))]
    [the-name branch]))

(defn convert! []
  (let [content (.readFileSync fs ir-path "utf8")
        ir (read-string content)
        map-of-ns (:namespaces ir)
        map-of-procs (:procedures ir)
        map-of-defs (:definitions ir)
        group-of-defs (->> map-of-defs
                           (group-by get-by-ns)
                           (map get-entry)
                           (into {}))
        files (->> map-of-ns
                    (map (fn [entry]
                            (let [[k v] entry]
                              [k {:ns v :defs (get group-of-defs k)
                                  :procs (or (get map-of-procs k) [])}])))
                    (into {}))
        result {:package (:package ir)
                :files files}
        generated-code (with-out-str (pprint result {:width 120}))]
    (.writeFileSync fs ir-path generated-code)))

(convert!)
