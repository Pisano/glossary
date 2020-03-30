(ns pisano-cx-glossary.effects
  (:require [re-frame.core :refer [reg-fx]]
            [pisano-cx-glossary.util :as util]))


(reg-fx
  :change-title!
  (fn [title]
    (util/change-title! title)))