(ns pisano-cx-glossary.core
  (:require
   [reagent.core :as reagent]
   [re-frame.core :as re-frame]
   [pisano-cx-glossary.navigation.events :as events]
   [pisano-cx-glossary.routes :as routes]
   [pisano-cx-glossary.navigation.views :as views]
   [pisano-cx-glossary.config :as config]
   ))


(defn dev-setup []
  (when config/debug?
    (println "dev mode")))

(defn init-centry
[]
(.init js/Sentry (clj->js {:dsn "https://d2764b45c15449eeae761378a04b07c7@sentry.io/1882745"})))

(defn ^:dev/after-load mount-root []
  (re-frame/clear-subscription-cache!)
  (reagent/render [views/main-panel]
                  (.getElementById js/document "app")))

(defn init []
  (routes/app-routes)
  (re-frame/dispatch-sync [::events/initialize-db])
  (dev-setup)
  (mount-root))
