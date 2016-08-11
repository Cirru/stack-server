
(ns stack-server.core
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [boot.core :refer :all]
            [ring.middleware.cors :refer [wrap-cors]]
            [stack-server.analyze :refer [collect-files]]
            [clojure.java.io :as io]))

(defn make-result [collection fileset]
  (let [file-dict (collect-files collection) tmp (tmp-dir!)]
    (doseq [entry file-dict]
      (let [file-path (io/file tmp (str (key entry) ".cljs"))]
        (io/make-parents file-path)
        (spit file-path (val entry))))
    (-> fileset (add-resource tmp) (commit!))))

(deftask
  start-stack-editor!
  []
  (fn [next-handler]
    (fn [fileset]
      (let [file-path "stack-sepal.edn"
            stack-sepal-ref (atom (read-string (slurp file-path)))
            editor-handler (fn [request]
                             (cond
                               (= (:request-method request) :get) {:headers
                                                                   {"Access-Control-Allow-Origin"
                                                                    (get-in
                                                                      request
                                                                      [:headers
                                                                       "origin"]),
                                                                    "Content-Type"
                                                                    "text/edn",
                                                                    "Access-Control-Allow-Methods"
                                                                    "GET POST"},
                                                                   :status
                                                                   200,
                                                                   :body
                                                                   (pr-str
                                                                     @stack-sepal-ref)}
                               (= (:request-method request) :post) (let 
                                                                     [new-content
                                                                      (slurp
                                                                        (:body
                                                                          request))]
                                                                     (reset!
                                                                       stack-sepal-ref
                                                                       (read-string
                                                                         new-content))
                                                                     (next-handler
                                                                       (make-result
                                                                         @stack-sepal-ref
                                                                         fileset))
                                                                     (spit
                                                                       file-path
                                                                       new-content)
                                                                     {:headers
                                                                      {"Access-Control-Allow-Origin"
                                                                       (get-in
                                                                         request
                                                                         [:headers
                                                                          "origin"]),
                                                                       "Content-Type"
                                                                       "text/edn",
                                                                       "Access-Control-Allow-Methods"
                                                                       "GET POST"},
                                                                      :status
                                                                      200,
                                                                      :body
                                                                      (pr-str
                                                                        @stack-sepal-ref)})
                               :else {:headers
                                      {"Access-Control-Allow-Origin"
                                       (get-in
                                         request
                                         [:headers "origin"]),
                                       "Content-Type" "text/plain",
                                       "Access-Control-Allow-Methods"
                                       "GET POST"},
                                      :status 404,
                                      :body "not defined."}))]
        (run-jetty editor-handler {:port 7010, :join? false})
        (next-handler (make-result @stack-sepal-ref fileset))))))
