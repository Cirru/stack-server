
(ns cirru.stack-server
  (:require        [cljs.reader :refer [read-string]]
                   [cljs.core.async :refer [<! >! timeout chan]]
                   [shallow-diff.patch :refer [patch]]
                   [stack-server.analyze :refer [generate-file ns->path]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(def fs (js/require "fs"))
(def path (js/require "path"))

(def ir-path "example/stack-sepal.ir")
(def out-folder "example/src/")
(def extname ".clj")

(def sepal-ref
  (atom (read-string (fs.readFileSync ir-path "utf8"))))

(defn rewrite-file! [content]
  (fs.writeFileSync ir-path content))

(defn write-source! [sepal-data]
  (let [pkg (:package sepal-data)]
    (doseq [entry (:files sepal-data)]
      (let [[ns-part file-info] entry
            file-name (str (ns->path pkg ns-part) extname)
            content (generate-file ns-part file-info)]
        (println "File compiled:" file-name)
        (fs.writeFileSync (path.join out-folder file-name) content)))))

(write-source! @sepal-ref)
