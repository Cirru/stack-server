
(ns stack-server.core
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [boot.core :refer :all]
            [ring.middleware.cors :refer [wrap-cors]]))

(deftask
  start-stack-editor!
  []
  (fn [next-handler]
    (fn [fileset]
      (let [file-path "stack-sepal.edn"
            stack-sepal-ref (atom (read-string (slurp file-path)))
            editor-handler (fn [request]
                             (let [new-content (slurp (:body request))]
                               (next-handler fileset)
                               (reset!
                                 stack-sepal-ref
                                 (read-string new-content))
                               (spit file-path new-content)
                               {:headers
                                {"Access-Control-Allow-Origin"
                                 (get-in request [:headers "origin"]),
                                 "Content-Type" "text/edn",
                                 "Access-Control-Allow-Methods"
                                 "GET POST"},
                                :status 200,
                                :body (pr-str @stack-sepal-ref)}))]
        (run-jetty editor-handler {:port 7010, :join? false})
        (next-handler fileset)))))

(deftask
  only-println!
  []
  (fn [next-handler]
    (fn [fileset] (println "only print...") (next-handler fileset))))
