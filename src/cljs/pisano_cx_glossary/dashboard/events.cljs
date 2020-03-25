(ns pisano-cx-glossary.dashboard.events
  (:require [re-frame.core :refer [reg-event-fx reg-event-db]]
            [pisano-cx-glossary.util :as util]
            [pisano-cx-glossary.dummy-data :refer [data]]
            [clojure.string :as str]))

(reg-event-fx
  :get-main-data
  (fn [{:keys [db]} _]
    {:http-xhrio (util/create-custom-request-map :get
                                                 "https://rehber.pisano.co/ghost/api/v2/content/pages/?key=7db4164ecd6a36e2e61eef1fae&include=tags"
                                                 :get-main-data-result-ok
                                                 :get-main-data-fail-on)}))

(reg-event-db
  :get-main-data-result-ok
  (fn [db [_ response]]
    (let [pages      (:pages response)
          new-data   (into pages data)
          filter-data (filterv #(= "cx-vocabulary" (-> % :primary_tag :slug)) new-data)
          group-data (group-by (fn [d] (some-> d :title (subs 0 1) util/upper-case)) filter-data)]
      (assoc db :data group-data))))

(reg-event-db
  :get-main-data-fail-on
  (fn [db _]
    db))

(reg-event-db
 :add-data
 (fn [db [_ coll val]]
  (assoc-in db coll val)))