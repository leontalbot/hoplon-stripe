(set-env!
 :dependencies '[[adzerk/boot-cljs           "1.7.228-1"]
                 [adzerk/boot-reload         "0.4.7"]
                 [clj-http                   "2.1.0"]
                 [cljsjs/stripe              "2.0-1"]
                 [compojure                  "1.5.0"]
                 [com.cognitect/transit-cljs "0.8.237"]
                 [hoplon/boot-hoplon         "0.1.13"]
                 [hoplon/castra              "3.0.0-SNAPSHOT"]
                 [hoplon/hoplon              "6.0.0-alpha14"]
                 [org.clojure/clojure        "1.7.0"]
                 [org.clojure/clojurescript  "1.8.51"]
                 [pandeiro/boot-http         "0.7.3"]
                 [ring                       "1.4.0"]
                 [ring/ring-defaults         "0.2.0"]]
  :resource-paths #{"assets" "src/clj"}
  :source-paths   #{"src/cljs" "src/hl"})

(require
  '[adzerk.boot-cljs      :refer [cljs]]
  '[adzerk.boot-reload    :refer [reload]]
  '[hoplon.boot-hoplon    :refer [hoplon prerender]]
  '[pandeiro.boot-http    :refer [serve]])

(deftask dev
  "Build hoplon-stripe for local development."
  []
  (comp
    (serve
      :port    8000
      :handler 'hoplon-stripe.handler/app
      :reload  true)
    (watch)
    (speak)
    (hoplon)
    (reload)
    (cljs)))

(deftask prod
  "Build hoplon-stripe for production deployment."
  []
  (comp
    (hoplon)
    (cljs :optimizations :advanced)
    (prerender)))

(deftask make-war
  "Build a war for deployment"
  []
  (comp (hoplon)
        (cljs :optimizations :advanced)
        (uber :as-jars true)
        (web :serve 'hoplon-stripe.handler/app)
        (war)))
