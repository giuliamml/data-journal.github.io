(ns blog.core
  (:require [markdown.core :as md]
            [org.satta.glob :as glob]
            [blog.template.layout :as layout]
            [hickory.render :as render]
            [hiccup.core :as hiccup]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.file :refer :all]
            [ring.middleware.reload :refer [wrap-reload]])
  (:gen-class))


(def pages (->> "/Users/fsousa/src/new-blog/pages/*.md"
                glob/glob first vector))

(def layout (layout/layout "some title" "some description"))



(->> pages first
     slurp
     md/md-to-html-string-with-meta :metadata keys)


(map (fn [page]
       (let [{:keys [html] {:keys [:title :subtitle :date]} :metadata} (->> page
                                                                            slurp
                                                                            md/md-to-html-string-with-meta)]
         subtitle)) pages)

(comment 
  (jade/configure {:pretty-print true})

  (->> "/Users/fsousa/src/new-blog/templates/post-layout.jade"
       jade/render
       (spit "/Users/fsousa/src/new-blog/templates/post-layout.html")))

(->> layout
     hiccup/html
     (spit "/Users/fsousa/src/new-blog/test.html"))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))

(def app
  (-> identity
      (wrap-file  "/Users/fsousa/src/new-blog")
      wrap-reload))

(comment
  (jetty/run-jetty app {:port 3000}))
