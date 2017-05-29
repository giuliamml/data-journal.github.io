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
            [clj-time.core :as t])
  (:gen-class))

(def root "/Users/fsousa/src/blog-engine")

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

(defn blog-as-data
  "Builds the blog data structure from the path of the markdown files of each page"
  [md-path]
  (->> (get-pages root)
       (reduce (fn [blog-map page]
                 (let [slug (->> page .getName (re-find #"([a-z\d-]+)") last keyword)
                       html-with-meta (-> page slurp (md/md-to-html-string-with-meta :inhibit-separator "%"))
                       {:keys [html] {:keys [:title :subtitle :date]} :metadata} html-with-meta
                       hiccup-content (->> html hickory/parse-fragment (mapv hickory/as-hiccup) concat (into [:div]))]
                   (-> blog-map
                       (assoc-in [slug :title]  (-> title first))
                       (assoc-in [slug :subtitle] (-> subtitle first))
                       (assoc-in [slug :date] (-> date first string->date))
                       (assoc-in [slug :content] hiccup-content)))) {})))
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
  (refresh)
  (let [blog-structure (-> root blog-as-data enrich-with-dates)]
    (->> (dissoc blog-structure :dates) ;;Iterate over pages only
         (map (fn [[slug-keyword {:keys [:title :subtitle :content]}]];TODO should be doseq because side effects
                (let [meta-title (str "Data Journal - " title)
                      menu (menu (:dates blog-structure))
                      modal-menu (modal-menu (:dates blog-structure))
                      full-page (layout meta-title subtitle content menu modal-menu twitter-el disqus-el)
                      path (str root "/" (name slug-keyword) ".html")
                      front-page (layout "Front-page"
                                         "front-page-description"
                                         (front-page-hiccup (dissoc blog-structure :dates))
                                         menu
                                         modal-menu
                                         nil nil)]
                  (spit path (hiccup/html full-page))
                  (spit (str root "/index.html") (hiccup/html front-page))))))))

(defn main
  [& args]
  (build! root))

