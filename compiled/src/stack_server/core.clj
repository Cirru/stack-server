
(ns stack-server.core
  (:require [ring.adapter.jetty :refer [run-jetty]]))

(println "script running")

(defn print-any [] (println "anything!"))

(defn handler [request]
  {:headers {"Content-Type" "text/edn"}, :status 200, :body "Demo"})

(defn run-server! [] (run-jetty handler {:port 7010}))
