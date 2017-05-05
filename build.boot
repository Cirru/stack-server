
(set-env!
  :resource-paths #{"src/" "polyfill/"}
  :dependencies '[[org.clojure/clojure       "1.8.0"       :scope "test"]
                  [org.clojure/clojurescript "1.9.521"     :scope "test"]
                  [adzerk/boot-cljs          "1.7.228-1"   :scope "test"]
                  [cumulo/shallow-diff       "0.1.3"       :scope "test"]
                  [fipp                      "0.6.9"       :scope "test"]
                  [andare                    "0.5.0"       :scope "test"]
                  [cirru/sepal               "0.0.17"]
                  [mvc-works/polyfill        "0.1.1"]])

(require '[adzerk.boot-cljs   :refer [cljs]])

(deftask build-simple []
  (comp
    ; (watch)
    (cljs :optimizations :simple
          :compiler-options {:language-in :ecmascript5
                             :target :nodejs
                             :parallel-build true})
    (target :no-clean true :dir #{"target"})))
