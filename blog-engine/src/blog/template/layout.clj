(ns blog.template.layout)

(defn layout [title description]
  [:html
   {:lang "en"}
   [:head
    [:meta
     {:content "width=device-width, initial-scale=1, maximum-scale=1" :name "viewport"}]
    [:meta {:name "description" :content description}]
    [:title title]
    [:link {:href "assets/css/font.css", :rel "stylesheet"}]
    [:link {:href "//maxcdn.bootstrapcdn.com/font-awesome/4.2.0/css/font-awesome.min.css" :rel "stylesheet"}]
    [:link {:href "//cdnjs.cloudflare.com/ajax/libs/highlight.js/8.4/styles/github.min.css" :rel "stylesheet"}]
    [:link {:type "text/css" :href "//cdn-images.mailchimp.com/embedcode/slim-081711.css" :rel "stylesheet"}]
    [:link {:href "assets/css/style.css" :rel "stylesheet"}]
    [:script {:type "text/javascript" :src "//cdn.mathjax.org/mathjax/latest/MathJax.js?config=TeX-AMS-MML_HTMLorMML"}]]

   [:body
    [:div#side-bar
     [:div#header

      [:div#title "Data Journal"]
      [:div#author [:div#name "Flávio Sousa"]

       [:div#social-media
        [:a {:target "_blank" :href "//pt.linkedin.com/pub/flávio-sousa/3a/a06/770/"}
         [:i.fa.fa-linkedin]]
        [:a {:target "_blank", :href "//twitter.com/verysocialsousa"}
         [:i.fa.fa-twitter]]
        [:a {:target "_blank", :href "//github.com/fjsousa"}
         [:i.fa.fa-github]]]]]

     [:div#menu
      [:a {:href "webrtc-part1.html"} [:span#nav-item-title "WebRTC Part 1"]]
      [:span#nav-item-date "(27-02-2015)"]
      [:br]
      [:a {:href "webrtc-part2.html"} [:span#nav-item-title "WebRTC Part 2"]]
      [:span#nav-item-date "(20-03-2015)"]
      [:br]
      [:a {:href "fgm.html"} [:span#nav-item-title "Cellular Automata"]]
      [:span#nav-item-date "(21-06-2015)"]
      [:br]
      [:a {:href "open-data.html"} [:span#nav-item-title "Open Data"]]
      [:span#nav-item-date "(09-10-2016)"]]

     [:div#footer-bar

      [:div#mc_embed_signup
       [:form#mc-embedded-subscribe-form.validate
        {:target "_blank"
         :action "//github.us10.list-manage.com/subscribe/post?u=5b26850668dc6b3f84778ca5e&id=cb5f4eedfe"
         :name "mc-embedded-subscribe-form"
         :novalidate "",
         :method "post"}
        [:div#mc_embed_signup_scroll
         [:label {:for "mce-EMAIL"} "Subscribe to the mailing list"]
         [:input#mce-EMAIL.email
          {:required "",
           :value "",
           :type "email",
           :placeholder "email address",
           :name "EMAIL"}]
         ;;real people should not fill this in and expect good things - do not remove this or risk form bot signups
         [:div
          {:style "position: absolute; left: -5000px;"}
          [:input
           {:value "",
            :type "text",
            :name "b_5b26850668dc6b3f84778ca5e_cb5f4eedfe",
            :tabindex "-1"}]]
         [:div.clear
          [:input#mc-embedded-subscribe.button
           {:value "Subscribe", :type "submit", :name "subscribe"}]]]]]

      [:div#about
       [:p#about-title "About:"]
       [:p "Data Journal is a blog about numbers, modelling and coding in general"]]]]

    [:div#container
     [:div#content

      ;;Content
      [:div :yld]
      ;;Content

      [:a.twitter-share-button
       {:href "https://twitter.com/share" :data-via "fjmarujo" :data-size "large"} "Tweet"
       [:script
        "!function(d,s,id){var js,fjs=d.getElementsByTagName(s)[0],p=/^http:/.test(d.location)?'http':'https';if(!d.getElementById(id)){js=d.createElement(s);js.id=id;js.src=p+'://platform.twitter.com/widgets.js';fjs.parentNode.insertBefore(js,fjs);}}(document, 'script', 'twitter-wjs');"]]

      [:div#disqus_thread
       [:script
        {:type "text/javascript"}
        "/* * * CONFIGURATION VARIABLES: EDIT BEFORE PASTING INTO YOUR WEBPAGE * * */\nvar disqus_shortname = 'this-data'; // required: replace example with your forum shortname\n/* * * DON'T EDIT BELOW THIS LINE * * */\n(function() {\n    var dsq = document.createElement('script'); dsq.type = 'text/javascript'; dsq.async = true;\n    dsq.src = '//' + disqus_shortname + '.disqus.com/embed.js';\n    (document.getElementsByTagName('head')[0] || document.getElementsByTagName('body')[0]).appendChild(dsq);\n})();"]
       [:noscript
        "Please enable JavaScript to view the"
        [:a
         {:href "https://disqus.com/?ref_noscript"}
         "comments powered by Disqus."]]]]]

    [:div#footer]
    [:script
     "(function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){\n  (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),\n  m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)\n  })(window,document,'script','//www.google-analytics.com/analytics.js','ga');\n  ga('create', 'UA-53583095-1', 'auto');\n  ga('send', 'pageview');\n"]
    [:script {:src "//code.jquery.com/jquery-1.11.2.min.js"}]
    [:script {:type "text/javascript" :src "//cdnjs.cloudflare.com/ajax/libs/highlight.js/8.4/highlight.min.js"}]
    [:script "hljs.initHighlightingOnLoad();"]]])
