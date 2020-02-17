(ns pisano-cx-glossary.dashboard.subs
  (:require [re-frame.core :as re-frame]))

(re-frame/reg-sub
  ::active-page
  (fn [db]
    (get (:data db) (:active-letter db))))

(re-frame/reg-sub
  ::active-letter
  (fn [db]
    (:active-letter db)))

(re-frame/reg-sub
  ::active-content-id
  (fn [db]
    (:active-content-id db)))

(re-frame/reg-sub
  ::active-content
  :<-[::active-page]
  :<-[::active-content-id]
  (fn [[active-page active-content-id] _]
    (some #(when (= active-content-id (:id %)) %) active-page)))


