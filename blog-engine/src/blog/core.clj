(ns blog.core
  (:require [markdown.core :as md]
            [org.satta.glob :as glob]
            [blog.template.layout :as layout]
            [hickory.render :as render]
            [hickory.core :as hickory]
            [hiccup.core :as hiccup]
            [clojure.walk :refer [prewalk]])
  (:gen-class))


(def pages (->> "/Users/fsousa/src/new-blog/pages/*.md"
                glob/glob first vector))

(def layout (first (layout/layout "some title" "some description")))

(defn render-full-hiccup
  "Receives a hiccup layout and content and returns the layout with the div #yield replaced by the content"
  [layout content]
  (prewalk #(if (=  % [:div :yld]) content %) layout))

(->> pages
     (map (fn [page]
            (let [slug (->> page .getName (re-find #"([a-z\d-]+)") last)
                  html-with-meta (->> page slurp md/md-to-html-string-with-meta)
                  {:keys [html] {:keys [:title :subtitle :date]} :metadata} html-with-meta
                  content (->> html hickory/parse-fragment (mapv hickory/as-hiccup) concat (into [:div]))
                  full-page (render-full-hiccup layout content)
                  path (str "/Users/fsousa/src/new-blog/" slug ".html"  )]
              #_full-page
              (spit path (hiccup/html full-page))))))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))

