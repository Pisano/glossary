(ns pisano-cx-glossary.dashboard.views
  (:require [reagent.core :as r]
            [re-frame.core :refer [dispatch dispatch-sync subscribe]]
            [pisano-cx-glossary.dashboard.events :as events]
            [pisano-cx-glossary.dashboard.subs :as subs]
            [pisano-cx-glossary.util :as util]))


(defn- html-render [content]
  [:div
   {:dangerouslySetInnerHTML
    {:__html content}}])


(defn- vocabulary-box-view [active-letter]
  [:div.glossary-aside
   [:a.glossary-logo
    [:img {:src "img/logo.png"}]]
   [:div.glossary-order
    [:div.glossary-order-inner
     (doall
       (map
         (fn [l]
           [:div.order-item
            {:on-click #(do
                          (dispatch-sync [:add-data [:active-letter] l])
                          (util/sleep (fn []
                                        (let [active-page    (->> @(subscribe [::subs/active-page]) (util/sort-by-locale :title) first)
                                              active-page-id (:id active-page)]
                                          (dispatch [:add-data :active-content-id active-page-id])
                                          (util/change-title! (:title active-page))))
                                      150))
             :class    (when (= l active-letter) "is-active")}
            l]) (or (util/sort-by-locale (keys @(subscribe [::subs/data])))
                    (map char (range 65 (inc 90))))))]]])


(defn- titles-view [active-letter page]
  [:div
   (doall
     (for [p (util/sort-by-locale :title page)]
       [:a.terms-item
        {:on-click #(do
                      (dispatch [:add-data :active-content-id (:id p)])
                      (util/change-title! (:title p)))
         :style    (when (= @(subscribe [::subs/active-content-id]) (:id p)) {:color "#2e81e8"})}
        (:title p)]))])


(defn- content-title-box-view [active-letter page active-content]
  [:div.glossary-terms
   [:div.glossary-terms-inner
    [:div.glossary-term-container
     [:div.term-image-a
      {:key (str "content-title-box-view-" active-letter)}]
     [titles-view active-letter page]]]])


(defn- main-box-view [page active-content]
  [:div.glossary-term-enrty
   [:div.term-header
    [:h1.term-title
     [html-render (:title active-content)]]]
   [:div.term-description
    [:div.container
     [html-render (:html active-content)]
     [html-render (:tag active-content)]]]])


(defn dashboard-view []
  (r/create-class
    {:component-did-mount #(dispatch [::events/get-pages])
     :reagent-render      (fn []
                            (let [page           @(subscribe [::subs/active-page])
                                  active-letter  @(subscribe [::subs/active-letter])
                                  active-content @(subscribe [::subs/active-content])]
                              [:div.glossary.glossary-entry.is-active
                               [vocabulary-box-view active-letter]
                               [content-title-box-view active-letter page active-content]
                               [main-box-view page active-content]]))}))