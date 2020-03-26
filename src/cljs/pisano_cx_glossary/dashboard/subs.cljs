(ns pisano-cx-glossary.dashboard.subs
  (:require [re-frame.core :refer [reg-sub]]
            [pisano-cx-glossary.util :as util]))


(reg-sub
  ::db
  (fn [db] db))


(reg-sub
  ::active-page
  :<- [::data]
  :<- [::active-letter]
  (fn [[data active-letter]]
    (distinct (get data active-letter))))


(reg-sub
  ::active-letter
  (fn [db]
    (:active-letter db)))


(reg-sub
  ::active-content-id
  (fn [db]
    (:active-content-id db)))


(reg-sub
  ::active-content
  :<- [::active-page]
  :<- [::active-content-id]
  (fn [[active-page active-content-id] _]
    (some #(when (= active-content-id (:id %)) %) active-page)))


(reg-sub
  ::data
  (fn [db]
    (let [pages       (:pages db)
          filter-data (filterv #(= "cx-vocabulary" (-> % :primary_tag :slug)) pages)
          group-data  (group-by (fn [d] (some-> d :title (subs 0 1) util/upper-case)) filter-data)]
      (into (sorted-map) group-data))))