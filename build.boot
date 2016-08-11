
(set-env!
 :dependencies '[[org.clojure/clojure       "1.8.0"       :scope "test"]
                 [cirru/boot-cirru-sepal    "0.1.9"       :scope "test"]
                 [adzerk/boot-test          "1.1.2"       :scope "test"]
                 [ring/ring-core            "1.5.0"]
                 [ring/ring-jetty-adapter   "1.5.0"]
                 [ring-cors                 "0.1.8"]
                 [cirru/sepal               "0.0.11"]
                 [clansi                    "1.0.0"]])

(set-env!
  :source-paths #{"compiled/src/"})

(require '[cirru-sepal.core   :refer [transform-cirru]]
         '[adzerk.boot-test   :refer :all]
         '[clojure.java.io    :as    io]
         '[stack-server.core  :refer [start-stack-editor! only-println!]])

(def +version+ "0.1.0")

(task-options!
  pom {:project     'cirru/stack-server
       :version     +version+
       :description "Server side toolchain for stack-editor"
       :url         "https://github.com/Cirru/boot-stack-server"
       :scm         {:url "https://github.com/Cirru/boot-stack-server"}
       :license     {"MIT" "http://opensource.org/licenses/mit-license.php"}})

(deftask compile-cirru []
  (set-env!
    :source-paths #{"cirru/"})
  (comp
    (transform-cirru)
    (target :dir #{"compiled/"})))

(deftask watch-compile []
  (set-env!
    :source-paths #{"cirru/"})
  (comp
    (watch)
    (transform-cirru)
    (target :dir #{"compiled/"})))

(deftask start-editor! []
  (comp
    (start-stack-editor!)
    (only-println!)))

(deftask build []
  (set-env!
    :source-paths #{"cirru/src"})
  (comp
    (transform-cirru)
    (pom)
    (jar)
    (install)
    (target)))

(deftask deploy []
  (set-env!
    :repositories #(conj % ["clojars" {:url "https://clojars.org/repo/"}]))
  (comp
    (build)
    (push :repo "clojars" :gpg-sign (not (.endsWith +version+ "-SNAPSHOT")))))

(deftask watch-test []
  (set-env!
    :source-paths #{"cirru/src" "cirru/test"})
  (comp
    (watch)
    (transform-cirru)
    (test :namespaces '#{stack-server.test})))
