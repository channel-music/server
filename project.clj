(defproject channel "0.1.0-SNAPSHOT"

  :description "A self-hosted music streaming application."
  :url "https://github.com/kalouantonis/channel"

  :repositories [["jaudiotagger" "https://dl.bintray.com/ijabz/maven"]]

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/tools.cli "0.3.5"]
                 [org.clojure/tools.logging "0.3.1"]
                 [clj-time "0.13.0"]
                 [cheshire "5.7.1"]
                 [cprop "0.1.10"]
                 [funcool/struct "1.0.0"]
                 [markdown-clj "0.9.99"]
                 [mount "0.1.11"]
                 ;; Web
                 [compojure "1.6.0"]
                 [luminus-immutant "0.2.3"]
                 [luminus-nrepl "0.1.4"]
                 [luminus/ring-ttl-session "0.3.2"]
                 [metosin/compojure-api "1.1.11"]
                 [metosin/ring-http-response "0.9.0"]
                 [ring/ring-core "1.6.2"]
                 [ring/ring-defaults "0.3.1"]
                 ;; Database
                 [hikari-cp "1.7.6"]
                 [to-jdbc-uri "0.3.0"]
                 [org.clojure/java.jdbc "0.7.0"]
                 [org.postgresql/postgresql "42.1.4"]
                 [migratus "0.9.9"]
                 [com.layerware/hugsql "0.4.7"]
                 ;; media files
                 [net.jthink/jaudiotagger "2.2.5"]
                 ;; helpful JVM utils
                 [org.apache.commons/commons-io "1.3.2"]]

  :min-lein-version "2.0.0"

  :jvm-opts ["-server" "-Dconf=.lein-env"]
  :source-paths ["src"]
  :test-paths ["test"]
  :resource-paths ["resources"]
  :target-path "target/%s/"
  :main ^:skip-aot channel.core

  :plugins [[lein-cprop "1.0.3"]
            [lein-immutant "2.1.0"]
            [migratus-lein "0.5.1"]]

  :migratus {:store :database :db ~(get (System/getenv) "DATABASE_URL")}

  :profiles
  {:uberjar {:omit-source true
             :aot :all
             :uberjar-name "channel.jar"
             :source-paths ["env/prod/clj"]
             :resource-paths ["env/prod/resources"]}

   :dev           [:project/dev :profiles/dev]
   :test          [:project/dev :project/test :profiles/test]

   :project/dev  {:dependencies [;; For multipart
                                 [org.apache.httpcomponents/httpmime "4.5.1"
                                  :exclusions [commons-logging]]
                                 [prone "1.1.4"]
                                 [pjstadig/humane-test-output "0.8.2"]
                                 [ring/ring-mock "0.3.0"]
                                 [ring/ring-devel "1.6.1"]]
                  :plugins      [[com.jakemccrary/lein-test-refresh "0.19.0"]]

                  :source-paths ["env/dev/clj"]
                  :resource-paths ["env/dev/resources"
                                   ;; For when tests are run in the dev environment
                                   "env/test/resources"]
                  :repl-options {:init-ns user}
                  :injections [(require 'pjstadig.humane-test-output)
                               (pjstadig.humane-test-output/activate!)]}
   :project/test {:resource-paths ["env/test/resources"]}
   :profiles/dev {}
   :profiles/test {}})
