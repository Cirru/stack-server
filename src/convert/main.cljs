(ns convert.main
  (:require [cljs.reader :refer [read-string]]
            [bisection-key.core :as bisection]))

(def user-id "root")
(def user-name "root")
(def shortid (js/require "shortid"))

(def timestamp (.valueOf (js/Date.)))

(def storage {:ir {},
              :sessions {},
              :users {user-id {:id user-id,
                               :name user-name,
                               :nickname user-name,
                               :password "",
                               :avatar nil}},
              :saved-files {},
              :configs {:storage-key "coir.edn",
                        :extension ".cljs",
                        :output "src",
                        :port 6001}})

(def fs (js/require "fs"))

(def ir (read-string (fs.readFileSync "ir.edn" "utf8")))

(defn cirru->tree [xs]
  (if (vector? xs)
    {:type :expr
     :id (.generate shortid)
     :time timestamp
     :data (loop [ys xs
                  result {}
                  next-id bisection/mid-id]
              (if (empty? ys) result
                (recur (rest ys)
                       (assoc result next-id (cirru->tree (first ys)))
                       (bisection/bisect next-id bisection/max-id))))}
    {:type :leaf
     :id (.generate shortid)
     :author user-id
     :time timestamp
     :text xs}))

(def new-ir
  (-> ir
    (update :files
      (fn [files]
        (->> files
          (map (fn [entry]
            (let [[k file] entry]
              [(str (:package ir) "." k) (-> file
                          (assoc :ns (cirru->tree (:ns file)))
                          (dissoc :procs)
                          (assoc :proc (cirru->tree (:procs file)))
                          (update :defs
                            (fn [defs]
                              (->> defs
                                (map (fn [def-entry]
                                        (let [[k expr] def-entry]
                                          [k (cirru->tree expr)])))
                                (into {})))))])))
          (into {}))))))

(defn convert! []
  (fs.writeFileSync "coir.edn" (pr-str (assoc storage :ir new-ir))))

(defn main! []
  (convert!)
  (println "converted!"))

(defn reload! []
  (println "reload!")
  (convert!))