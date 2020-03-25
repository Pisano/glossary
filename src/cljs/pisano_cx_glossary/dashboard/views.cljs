(ns pisano-cx-glossary.dashboard.views
 (:require [reagent.core :as r]
           [re-frame.core :refer [dispatch subscribe]]
           [pisano-cx-glossary.dashboard.subs :as subs]))

(defn html-render [content]
  [:div
   {:dangerouslySetInnerHTML
    {:__html content}}])

(defn vocabulary-box-view [active-letter]
  [:div.glossary-aside
   [:a.glossary-logo
    [:img {:src "img/logo.png"}]]
   [:div.glossary-order
    [:div.glossary-order-inner
     (doall
      (map
        (fn [l]
          [:div.order-item
           {:on-click #(dispatch [:add-data [:active-letter] l])
            :class (when (= l active-letter) "is-active")}
           l]) (or (keys @(subscribe [::subs/data]))
                   (map char (range 65 (inc 90))))))]]])

(defn titles-view [active-letter page]
  [:div
   (doall
     (for [p (sort-by :title page)]
       [:a.terms-item
        {:on-click #(dispatch [:add-data [:active-content-id] (:id p)])}
        (:title p)]))])

(defn content-title-box-view [active-letter page active-content]
  [:div.glossary-terms
    [:div.glossary-terms-inner
      [:div.glossary-term-container
        [:div.term-image-a
          {:key (str "content-title-box-view-" active-letter)}]
        [titles-view active-letter page]]]])

(defn main-box-view [page active-content]
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
      {:component-did-mount #(dispatch [:get-main-data])
       :reagent-render (fn []
                          (let [page           @(subscribe[::subs/active-page])
                                active-letter  @(subscribe[::subs/active-letter])
                                active-content @(subscribe[::subs/active-content])]
                            [:div.glossary.glossary-entry.is-active
                             [vocabulary-box-view active-letter]
                             [content-title-box-view active-letter page active-content]
                             [main-box-view page active-content]]))}))