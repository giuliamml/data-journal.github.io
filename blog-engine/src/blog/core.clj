(ns blog.core
  (:require [markdown.core :as md]
            [org.satta.glob :as glob]
            [blog.template.layout :as layout]
            [hickory.render :as render]
            [hickory.core :as hickory]
            [hiccup.core :as hiccup]
            [clojure.walk :refer [prewalk]]
            [clojure.string :as string]
            [clj-time.core :as t])
  (:gen-class))

(def root "/Users/fsousa/src/blog-engine")

(defn get-pages [root] (->> (str root "/pages/*.md") glob/glob))

(def layout (layout/layout "some title" "some description"))

(defn render-content-in-full-hiccup
  "Receives a hiccup layout and content and returns the layout with the div #yield replaced by the content"
  [layout content]
  (prewalk #(if (=  % [:div :yld]) content %) layout))

(defn blog-as-data
  "Builds the blog data structure from the path of the markdown files of each page"
  [md-path]
  (->> (get-pages root)
       (reduce (fn [blog-map page]
                 (let [slug (->> page .getName (re-find #"([a-z\d-]+)") last keyword)
                       html-with-meta (->> page slurp md/md-to-html-string-with-meta)
                       {:keys [html] {:keys [:title :subtitle :date]} :metadata} html-with-meta
                       hiccup-content (->> html hickory/parse-fragment (mapv hickory/as-hiccup) concat (into [:div]))]
                   (-> blog-map
                       (assoc-in [slug :title]  (-> title first))
                       (assoc-in [slug :subtitle] (-> subtitle first))
                       (assoc-in [slug :date] (-> date first))
                       (assoc-in [slug :content] hiccup-content)))) {})))

(defn slug->short-title
  [slug]
  (->> (string/split (name slug) #"-")
       (map string/capitalize)
       (string/join " ")))

(defn string->date
  [string]
  (->> (string/split string #" ")
       (map #(Integer. %))
       (apply t/date-time)))

(string->date "2016 9 10")

(defn enrich-with-dates
  "receives the blog data structure and enriches it with date info. Basically bringing the date
   of each post to the the root level"
  [blog]
  (let [dates (->> blog
                   (map (fn [[k {:keys [:date]}]] {:slug k :date (string->date date) :short-name (slug->short-title k)}))
                   (sort-by :date))]
    (assoc blog :dates dates)))

(-> root
    blog-as-data
    enrich-with-dates
    :dates
    layout/menu)


(->> pages
     (map (fn [page]
            (let [slug (->> page .getName (re-find #"([a-z\d-]+)") last)
                  html-with-meta (->> page slurp md/md-to-html-string-with-meta)
                  {:keys [html] {:keys [:title :subtitle :date]} :metadata} html-with-meta
                  content (->> html hickory/parse-fragment (mapv hickory/as-hiccup) concat (into [:div]))
                  full-page (render-content-in-full-hiccup (layout/layout (first title) (first subtitle)) content)
                  path (str root "/" slug ".html"  )]
              #_full-page
              (spit path (hiccup/html full-page))))))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))

