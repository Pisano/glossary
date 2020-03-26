(ns pisano-cx-glossary.dashboard.subs
  (:require [re-frame.core :refer [reg-sub]]
            [pisano-cx-glossary.util :as util]
            [clojure.set :as set]))

(def alphabet ["A", "B", "C", "Ç", "D", "E", "F", "G", "Ğ", "H", "I", "İ", "J", "K", "L", "M", "N", "O", "Ö", "P", "R", "S", "Ş", "T", "U", "Ü", "V", "Y", "Z"])

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
    (let [pages              (:pages db)
          filter-data        (filterv #(= "cx-vocabulary" (-> % :primary_tag :slug)) pages)
          group-data         (group-by (fn [d] (some-> d :title (subs 0 1) util/upper-case)) filter-data)
          key-exists         (keys group-data)
          key-does-not-exist (set/difference (set alphabet) (set key-exists))
          dummy-map          (apply merge (map #(hash-map %1 nil) key-does-not-exist))]
      (into (sorted-map) (merge group-data dummy-map)))))