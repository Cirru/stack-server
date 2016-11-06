
(ns stack-server.core
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [boot.core :refer :all]
            [ring.middleware.cors :refer [wrap-cors]]
            [stack-server.analyze :refer [collect-files]]
            [clojure.java.io :as io]
            [shallow-diff.patch :refer [patch]]))

(defn make-result [collection fileset extname]
  (let [file-dict (collect-files collection), tmp (tmp-dir!)]
    (doseq [entry file-dict]
      (let [file-path (io/file tmp (str (key entry) (if (some? extname) extname ".cljs")))]
        (io/make-parents file-path)
        (spit file-path (val entry))))
    (-> fileset (add-resource tmp) (commit!))))

(deftask
 start-stack-editor!
 [p port int "Port" e extname VAL str "Extname" f filename VAL str "Filename"]
 (fn [next-handler]
   (fn [fileset]
     (let [file-path (or filename "stack-sepal.ir")
           stack-sepal-ref (atom (read-string (slurp file-path)))
           editor-handler (fn [request]
                            (let [cors-headers {"Access-Control-Allow-Origin" (get-in
                                                                               request
                                                                               [:headers
                                                                                "origin"]),
                                                "Content-Type" "text/edn; charset=UTF-8",
                                                "Access-Control-Allow-Methods" "GET, POST, PATCH, OPTIONS"}]
                              (cond
                                (= (:request-method request) :get)
                                  {:headers (merge cors-headers),
                                   :status 200,
                                   :body (pr-str @stack-sepal-ref)}
                                (= (:request-method request) :post)
                                  (let [new-content (slurp (:body request))
                                        sepal-data (read-string new-content)
                                        result (make-result sepal-data fileset extname)]
                                    (comment println "writing file:" file-path new-content)
                                    (spit file-path new-content)
                                    (binding [*warnings* (atom 0)] (next-handler result))
                                    (reset! stack-sepal-ref sepal-data)
                                    {:headers (merge cors-headers),
                                     :status 200,
                                     :body (pr-str {:status "ok"})})
                                (= (:request-method request) :patch)
                                  (let [changes-content (slurp (:body request))
                                        changes (read-string changes-content)
                                        new-sepal-data (patch @stack-sepal-ref changes)
                                        result (make-result new-sepal-data fileset extname)]
                                    (comment println "writing file:" file-path new-content)
                                    (spit file-path (pr-str new-sepal-data))
                                    (binding [*warnings* (atom 0)] (next-handler result))
                                    (reset! stack-sepal-ref new-sepal-data)
                                    {:headers (merge cors-headers),
                                     :status 200,
                                     :body (pr-str {:status "ok"})})
                                (= (:request-method request) :options)
                                  {:headers (merge cors-headers), :status 200, :body "ok"}
                                :else
                                  {:headers (merge cors-headers),
                                   :status 404,
                                   :body (pr-str {:status "ok"})})))]
       (run-jetty editor-handler {:port (or port 7010), :join? false})
       (next-handler (make-result @stack-sepal-ref fileset extname))))))

(deftask
 transform-stack
 [e extname VAL str "Extname" f filename VAL str "Filename"]
 (fn [next-handler]
   (fn [fileset]
     (let [file-path (or filename "stack-sepal.ir")
           stack-sepal (read-string (slurp file-path))]
       (next-handler (make-result stack-sepal fileset extname))))))
