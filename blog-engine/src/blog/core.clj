(ns blog.core
  (:require [markdown.core :as md]
            [org.satta.glob :as glob]
            [blog.template.layout :refer :all]
            [hickory.render :as render]
            [hickory.core :as hickory]
            [hiccup.core :as hiccup]
            [clojure.tools.namespace.repl :refer [refresh]]
            [clojure.walk :refer [prewalk]]
            [clojure.string :as string]
            [clj-time.core :as t]
            [clojure-watch.core :refer [start-watch]])
  (:gen-class))

(def root "/Users/fsousa/src/blog-engine")
(def base-url "http://datajournal.co.uk")

(defn get-pages [root] (->> (str root "/pages/*.md") glob/glob))

(defn render-content-in-full-hiccup
  "Receives a hiccup layout and content and returns the layout with the div #yield replaced by the content"
  [layout content]
  (prewalk #(if (=  % [:div :yld]) content %) layout))

(defn string->date
  [string]
  (if string
    (->> (string/split string #" ")
         (map #(Integer. %))
         (apply t/date-time))
    (prn "Error: Date string is nil")))

(defn string->tags
  [string]
  (if string
    (-> string first (string/split #","))
    (prn "Error: Tags string is nil")))


(defn string->thumb-hiccup [string]
  (when string
    (->> string first
         hickory/parse-fragment
         (map hickory/as-hiccup))))

(defn blog-as-data
  "Builds the blog data structure from the path of the markdown files of each page"
  [md-path]
  (->> (get-pages root)
       (reduce (fn [blog-map page]
                 (let [slug (->> page .getName (re-find #"([a-z\d-]+)") last keyword)
                       html-with-meta (-> page slurp (md/md-to-html-string-with-meta :inhibit-separator "%"))
                       {:keys [html] {:keys [:title :subtitle :date :tags :thumb]} :metadata} html-with-meta
                       hiccup-content (->> html hickory/parse-fragment (mapv hickory/as-hiccup) concat (into [:div]))]
                   (-> blog-map
                       (assoc-in [slug :title]  (-> title first))
                       (assoc-in [slug :subtitle] (-> subtitle first))
                       (assoc-in [slug :date] (-> date first string->date))
                       (assoc-in [slug :content] hiccup-content)
                       (assoc-in [slug :tags] (string->tags tags))
                       (assoc-in [slug :thumb] (string->thumb-hiccup thumb))))) {})))
;;TODO validate output with spec. Each markdown should have a date, title and subtitle

(defn slug->short-title
  [slug]
  (->> (string/split (name slug) #"-")
       (map string/capitalize)
       (string/join " ")))

(defn enrich-with-dates
  "receives the blog data structure and enriches it with date info. Basically bringing the date
   of each post to the the root level"
  [blog]
  (let [dates (->> blog
                   (map (fn [[k {:keys [:date]}]] {:slug k :date date :short-name (slug->short-title k)}))
                   (sort-by :date))]
    (assoc blog :dates dates)))

(defn build! [root]
  ;;(refresh);;reloads namespaces that have been changed
  (let [page-structure (blog-as-data root)
        full-blog-structure (enrich-with-dates page-structure)
        menu (menu (:dates full-blog-structure))
        modal-menu (modal-menu (:dates full-blog-structure))
        front-page (layout index-title
                           index-description
                           (front-page-hiccup page-structure)
                           menu
                           modal-menu
                           nil nil)
        sitemap (sitemap base-url page-structure)
        rss-feed (rss-feed base-url page-structure)]
    (doseq [[slug-keyword {:keys [:title :subtitle :content]}] page-structure]
      (let [meta-title (str "Data Journal - " title)
            full-page (layout meta-title subtitle content menu modal-menu twitter-el disqus-el)
            path (str root "/" (name slug-keyword) ".html")]
        (spit path (hiccup/html full-page))))
    (spit (str root "/index.html") (hiccup/html front-page))
    (spit (str root "/sitemap.txt") sitemap)
    (spit (str root "/feed.xml") rss-feed)))

(defn -main
  [& args]
  (start-watch [{:path  (str root "/pages/")
               :event-types [:create :modify :delete]
               :bootstrap (fn [path] (println "Starting to watch " path))
               :callback (fn [event filename] (do (build! root)
                                                  (prn (str "File " filename " with event " event))))
               :options {:recursive true}}]))
