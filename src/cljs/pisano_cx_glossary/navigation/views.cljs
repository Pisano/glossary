(ns pisano-cx-glossary.navigation.views
  (:require [re-frame.core :as re-frame]
            [pisano-cx-glossary.navigation.subs :as subs]
            [pisano-cx-glossary.dashboard.views :refer [dashboard-view]]))

(defn- panels [panel-name]
  (case panel-name
    :dashboard-panel [dashboard-view]
    [:div]))

(defn show-panel [panel-name]
  [panels panel-name])

(defn main-panel []
  (let [active-panel (re-frame/subscribe [::subs/active-panel])]
    [show-panel @active-panel]))
