
(set-env!
  :source-paths #{"src/"}
  :dependencies '[[org.clojure/clojure       "1.8.0"       :scope "test"]
                  [org.clojure/clojurescript "1.9.293"     :scope "test"]
                  [adzerk/boot-test          "1.1.2"       :scope "test"]
                  [cirru/sepal               "0.0.15"]
                  [andare                    "0.4.0"]
                  [cumulo/shallow-diff       "0.1.1"]])

(require '[adzerk.boot-test   :refer :all]
         '[clojure.java.io    :as    io])

(def +version+ "0.1.24")

(task-options!
  pom {:project     'cirru/boot-stack-server
       :version     +version+
       :description "Server side toolchain for stack-editor"
       :url         "https://github.com/Cirru/boot-stack-server"
       :scm         {:url "https://github.com/Cirru/boot-stack-server"}
       :license     {"MIT" "http://opensource.org/licenses/mit-license.php"}})

(deftask build []
  (comp
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
    :source-paths #{"src" "test"})
  (comp
    (watch)
    (test :namespaces '#{stack-server.test})))
