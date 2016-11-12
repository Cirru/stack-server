
(ns stack-server.core
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [boot.core :refer :all]
            [ring.middleware.cors :refer [wrap-cors]]
            [stack-server.analyze :refer [collect-files]]
            [clojure.java.io :as io]
            [shallow-diff.patch :refer [patch]]))

(defn make-header [request]
  {"Access-Control-Allow-Origin" (get-in request [:headers "origin"]),
   "Content-Type" "text/edn; charset=UTF-8",
   "Access-Control-Allow-Methods" "GET, POST, PATCH, OPTIONS"})

(defn response [code headers body] {:headers headers, :status code, :body body})

(defn make-result [collection fileset extname]
  (let [file-dict (collect-files collection), tmp (tmp-dir!)]
    (doseq [entry file-dict]
      (let [file-path (io/file tmp (str (key entry) (if (some? extname) extname ".cljs")))]
        (io/make-parents file-path)
        (spit file-path (val entry))))
    (-> fileset (add-resource tmp) (commit!))))

(defn respond [file-path
               new-content
               next-handler
               fileset
               extname
               sepal-ref
               sepal-data
               request]
  (try
   (let [result (make-result sepal-data fileset extname)]
     (comment println "writing file:" file-path new-content)
     (spit file-path new-content)
     (binding [*warnings* (atom 0)] (next-handler result))
     (reset! sepal-ref sepal-data)
     {:headers (merge (make-header request)), :status 200, :body (pr-str {:status "ok"})})
   (catch
    Exception
    e
    (do
     (.printStackTrace e)
     (println "Error Message:" (.getMessage e))
     (response 406 (make-header request) (pr-str {:status (.getMessage e)}))))))

(deftask
 start-stack-editor!
 [p port int "Port" e extname VAL str "Extname" f filename VAL str "Filename"]
 (fn [next-handler]
   (fn [fileset]
     (let [file-path (or filename "stack-sepal.ir")
           sepal-ref (atom (read-string (slurp file-path)))]
       (run-jetty
        (fn [request]
          (cond
            (= (:request-method request) :get)
              {:headers (merge (make-header request)), :status 200, :body (pr-str @sepal-ref)}
            (= (:request-method request) :post)
              (let [raw-sepal (slurp (:body request)), sepal-data (read-string raw-sepal)]
                (respond
                 file-path
                 raw-sepal
                 next-handler
                 fileset
                 extname
                 sepal-ref
                 sepal-data
                 request))
            (= (:request-method request) :patch)
              (let [changes-content (slurp (:body request))
                    changes (read-string changes-content)
                    sepal-data (patch @sepal-ref changes)
                    raw-sepal (pr-str sepal-data)]
                (respond
                 file-path
                 raw-sepal
                 next-handler
                 fileset
                 extname
                 sepal-ref
                 sepal-data
                 request))
            (= (:request-method request) :options) (response 200 (make-header request) "ok")
            :else (response 404 (make-header request) (pr-str {:status "ok"}))))
        {:port (or port 7010), :join? false})
       (next-handler (make-result @sepal-ref fileset extname))))))

(deftask
 transform-stack
 [e extname VAL str "Extname" f filename VAL str "Filename"]
 (fn [next-handler]
   (fn [fileset]
     (let [file-path (or filename "stack-sepal.ir")
           stack-sepal (read-string (slurp file-path))]
       (next-handler (make-result stack-sepal fileset extname))))))
