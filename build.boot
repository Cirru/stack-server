
(set-env!
  :source-paths #{"src/"}
  :dependencies '[[org.clojure/clojure       "1.8.0"       :scope "test"]
                  [org.clojure/clojurescript "1.9.293"     :scope "test"]
                  [adzerk/boot-test          "1.1.2"       :scope "test"]
                  [ring/ring-core            "1.5.0"]
                  [ring/ring-jetty-adapter   "1.5.0"]
                  [ring-cors                 "0.1.8"]
                  [cirru/sepal               "0.0.15"]
                  [andare                    "0.4.0"]
                  [cumulo/shallow-diff       "0.1.1"]])

(require '[adzerk.boot-test   :refer :all]
         '[clojure.java.io    :as    io]
         '[stack-server.core  :refer [start-stack-editor! transform-stack]])

(def +version+ "0.1.24")

(task-options!
  pom {:project     'cirru/boot-stack-server
       :version     +version+
       :description "Server side toolchain for stack-editor"
       :url         "https://github.com/Cirru/boot-stack-server"
       :scm         {:url "https://github.com/Cirru/boot-stack-server"}
       :license     {"MIT" "http://opensource.org/licenses/mit-license.php"}})

(deftask dev! []
  (comp
    (wait)
    (start-stack-editor! :port 7010 :extname ".clj" :filename "stack-sepal.ir")
    (target :dir #{"src/"})))

(deftask demo! []
  (comp
    (wait)
    (start-stack-editor! :port 7011 :extname ".clj" :filename "example/stack-sepal.ir")
    (target :dir #{"example/src/"})))

(deftask generate-code []
  (comp
    (transform-stack :extname ".clj" :filename "stack-sepal.ir")
    (target :dir #{"src/"})))

(deftask build []
  (comp
    (transform-stack :extname ".clj" :filename "stack-sepal.ir")
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
