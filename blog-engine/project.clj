(defproject blog "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [markdown-clj "0.9.99"]
                 [clj-glob "1.0.0"]
                 [hickory "0.7.1"]
                 [hiccup "1.0.5"]
                 [ring "1.6.0-RC3"]
                 [clj-time "0.13.0"]
                 [org.clojure/tools.namespace "0.2.11"]
                 [clj-rss "0.2.3"]
                 [clojure-watch "LATEST"]]
  :plugins [[com.billpiel/sayid "0.0.15"]]
  :main ^:skip-aot blog.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
