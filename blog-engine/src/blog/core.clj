(ns blog.core
  (:require [markdown.core :as md]
            [clj-jade.core :as jade]
            [org.satta.glob :as glob])
  (:gen-class))


(def pages (->> "/Users/fsousa/src/new-blog/pages/*.md"
                glob/glob first vector))

(->> pages first
     slurp
     md/md-to-html-string-with-meta :metadata keys)


(map (fn [page]
       (let [{:keys [html] {:keys [:title :subtitle :date]} :metadata} (->> page
                                                                            slurp
                                                                            md/md-to-html-string-with-meta)]
         subtitle)) pages)

(jade/configure {:pretty-print true})

(->> "/Users/fsousa/src/new-blog/templates/post-layout.jade"
     jade/render
     (spit "/Users/fsousa/src/new-blog/templates/post-layout.html"))



(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))

