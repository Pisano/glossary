
; Disabled for now, we are not using ROUTES NAMESPACE!!!
; getting failed to load shadow.module.app.append.js Error: No protocol method IMapEntry.-key defined for type cljs.core/LazySeq: (:* "/ses/5e7ddd6bd6c6207e629c5ee8")

(ns pisano-cx-glossary.routes
  (:require-macros [secretary.core :refer [defroute]])
  (:import [goog History]
           [goog.history EventType])
  (:require
   [secretary.core :as secretary]
   [goog.events :as gevents]
   [re-frame.core :as re-frame]
   [pisano-cx-glossary.navigation.events :as events]
   [pisano-cx-glossary.dashboard.events]))

(defn hook-browser-navigation! []
  (doto (History.)
    (gevents/listen
     EventType/NAVIGATE
     (fn [event]
       (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

(defn app-routes []
  (secretary/set-config! :prefix "#")
  ;; --------------------
  ;; define routes here
  (defroute "/" [] (re-frame/dispatch [::events/set-active-panel :dashboard-panel]))

  (defroute "*" [] (re-frame/dispatch [::events/set-active-panel :dashboard-panel]))

  ;; --------------------
  (hook-browser-navigation!))
