(ns pisano-cx-glossary.dashboard.events
  (:require [re-frame.core :refer [reg-event-fx reg-event-db]]
            [pisano-cx-glossary.util :as util]
            [pisano-cx-glossary.effects]))

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
                                                 [::get-page-data-result-fail n])}))


(reg-event-db
  ::get-page-data-result-ok
  (fn [db [_ response]]
    (let [db (update db :pages concat (:pages response))]
      (assoc db :meta (-> response :meta :pagination (select-keys [:total :pages :limit]))))))


(reg-event-fx
  ::get-page-data-result-fail
  (fn [_ [_ n]]
    {:dispatch-later [{:ms 1000 :dispatch [::get-page-data n]}]}))


(reg-event-db
  :add-data
  (fn [db [_ coll val]]
    (assoc-in db (if (vector? coll) coll [coll]) val)))


(reg-event-fx
  ::find-and-set-content-by-id
  (fn [{:keys [db]} [_ id]]
    (if-let [content (some #(when (= id (:id %)) %) (:pages db))]
      (let [db (assoc db :active-content-id id)]
        {:db            (assoc db :active-letter (some-> content :title util/upper-case first str))
         :change-title! (:title content)})
      {:dispatch-later [{:ms 150 :dispatch [::find-and-set-content-by-id id]}]})))

(reg-event-fx
  ::select-first-content
  (fn [{:keys [db]} _]
    (cond
      (and (-> db :active-content-id not)
           (= (count (:pages db)) (-> db :meta :total))) (let [pages       (:pages db)
                                                               filter-data (filterv #(= "cx-vocabulary" (-> % :primary_tag :slug)) pages)
                                                               group-data  (group-by (fn [d] (some-> d :title (subs 0 1) util/upper-case)) filter-data)
                                                               contents    (get group-data "A")
                                                               f-content   (first (util/sort-by-locale :title contents))]
                                                           {:dispatch [::find-and-set-content-by-id (:id f-content)]})

      (and (-> db :active-content-id not)
           (not= (count (:pages db)) (-> db :meta :total))) {:dispatch-later [{:ms 200 :dispatch [::select-first-content]}]}

      :else {:db db})))
