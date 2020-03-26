(ns pisano-cx-glossary.dashboard.events
  (:require [re-frame.core :refer [reg-event-fx reg-event-db]]
            [pisano-cx-glossary.util :as util]))

(def datasource-url "https://rehber.pisano.co/ghost/api/v2/content/pages/?key=7db4164ecd6a36e2e61eef1fae&include=tags")


(reg-event-fx
  ::get-pages
  (fn [_ _]
    {:http-xhrio (util/create-custom-request-map :get datasource-url
                                                 ::get-pages-count-result-ok)}))


(reg-event-fx
  ::get-pages-count-result-ok
  (fn [{:keys [db]} [_ response]]
    (let [page-count (-> response :meta :pagination :pages)]
      {:db         (assoc db :page-count page-count)
       :dispatch-n (reduce #(conj %1 [::get-page-data %2]) [] (range 1 (inc page-count)))})))


(reg-event-fx
  ::get-page-data
  (fn [_ [_ n]]
    {:http-xhrio (util/create-custom-request-map :get (str datasource-url "&page=" n)
                                                 ::get-page-data-result-ok
                                                 ::get-page-data-result-fail)}))


(reg-event-db
  ::get-page-data-result-ok
  (fn [db [_ response]]
    (update db :pages concat (:pages response))))


(reg-event-db
  ::get-page-data-result-fail
  (fn [db _]
    db))


(reg-event-db
  :add-data
  (fn [db [_ coll val]]
    (assoc-in db (if (vector? coll) coll [coll]) val)))