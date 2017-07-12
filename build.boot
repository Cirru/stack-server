
(set-env!
  :resource-paths #{"src/" "polyfill/"}
  :dependencies '[[cirru/sepal "0.0.18"]
                  [mvc-works/polyfill "0.1.1"]])

(def +version+ "0.2.9")

(deftask build []
  (comp
    (pom :project     'cirru/stack-server
         :version     +version+
         :description "Workflow"
         :url         "https://github.com/Cirru/stack-server"
         :scm         {:url "https://github.com/Cirru/stack-server"}
         :license     {"MIT" "http://opensource.org/licenses/mit-license.php"})
    (jar)
    (install)
    (target)))

(deftask deploy []
  (set-env!
    :repositories #(conj % ["clojars" {:url "https://clojars.org/repo/"}]))
  (comp
    (build)
    (push :repo "clojars" :gpg-sign (not (.endsWith +version+ "-SNAPSHOT")))))
