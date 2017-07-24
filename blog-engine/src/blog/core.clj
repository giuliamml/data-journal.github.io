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
                 (let [root (->> page .getName (re-find #"([a-z\d-]+)") last keyword)
                       html-with-meta (-> page slurp (md/md-to-html-string-with-meta :inhibit-separator "%"))
                       {:keys [html] {:keys [:title :subtitle :date :tags :thumb :slug]} :metadata} html-with-meta
                       hiccup-content (->> html hickory/parse-fragment (mapv hickory/as-hiccup) concat (into [:div]))]
                   (-> blog-map
                       (assoc-in [root :title]  (-> title first))
                       (assoc-in [root :subtitle] (-> subtitle first))
                       (assoc-in [root :date] (-> date first string->date))
                       (assoc-in [root :content] hiccup-content)
                       (assoc-in [root :tags] (string->tags tags))
                       (assoc-in [root :thumb] (string->thumb-hiccup thumb))
                       (assoc-in [root :slug] (first slug))))) {})))
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
                   (sort-by :date)
                   reverse)]
    (assoc blog :dates dates)))

(defn append-clojure-language-pitfalls!
  [root]
  (let [page (str root "/clojure-language-pitfalls.html")
        prefix "---\nredirect_from: \"/language-fundamentalism.html\"\n---\n"]
    (spit page (->> page slurp (str prefix)))))

(defn custom!
  "Defines a bunch of custom rewrites"
  [root]
  (append-clojure-language-pitfalls! root))

(defn build! [root]
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
    (doseq [[k {:keys [:title :subtitle :content :slug]}] page-structure]
      (let [meta-title (str "Data Journal - " title)
            full-page (layout meta-title subtitle content menu modal-menu twitter-el disqus-el)
            path (str root "/" slug ".html")]
        (spit path (hiccup/html full-page))))
    (spit (str root "/index.html") (hiccup/html front-page))
    (spit (str root "/sitemap.txt") sitemap)
    (spit (str root "/feed.xml") rss-feed)
    (custom! root)))


(defn watch-fn []
  (println "\nBuilding all the things...")
  (build! root)
  (start-watch [{:path  (str root "/pages/")
                 :event-types [:create :modify :delete]
                 :bootstrap (fn [path] (println "Starting to watch " path))
                 :callback (fn [event filename] (do (build! root)
                                                    (println (str "File " filename " with event " event))))
                 :options {:recursive true}}]))

(watch-fn)

(defn -main
  [& args]
  (watch-fn))

