
(defn read-password [guide]
  (String/valueOf (.readPassword (System/console) guide nil)))

(set-env!
  :resource-paths #{"src/" "polyfill"}
  :dependencies '[[cirru/sepal        "0.1.0"]
                  [mvc-works/polyfill "0.1.1"]]
  :repositories #(conj % ["clojars" {:url "https://clojars.org/repo/"
                                     :username "jiyinyiyong"
                                     :password (read-password "Clojars password: ")}]))

(def +version+ "0.3.0")

(deftask deploy []
  (comp
    (pom :project     'cirru/stack-server
         :version     +version+
         :description "Stack server libraries"
         :url         "https://github.com/Cirru/stack-server"
         :scm         {:url "https://github.com/Cirru/stack-server"}
         :license     {"MIT" "http://opensource.org/licenses/mit-license.php"})
    (jar)
    (push :repo "clojars" :gpg-sign false)))
