(ns pisano-cx-glossary.dashboard.views
  (:require [reagent.core :as r]
            [re-frame.core :refer [dispatch dispatch-sync subscribe]]
            [pisano-cx-glossary.dashboard.events :as events]
            [pisano-cx-glossary.dashboard.subs :as subs]
            [pisano-cx-glossary.util :as util]
            [clojure.string :as str]))


(defn- html-render [content]
  [:div
   {:dangerouslySetInnerHTML
    {:__html content}}])


(defn- change-uri!
  [title id]
  (util/push-state (str "/#/" (some-> title
                                      util/lower-case
                                      str/trim
                                      (str/replace #"\s+" "-")
                                      (str/replace #"/+" "-")
                                      (str/replace #"-+" "-")) "/" id)))


(defn select-content
  [title id]
  (dispatch [::events/find-and-set-content-by-id id])
  (change-uri! title id))


(defn- vocabulary-box-view [active-letter]
  [:div.glossary-aside
   [:a.glossary-logo
    [:img {:src "img/logo.png"}]]
   [:div.glossary-order
    [:div.glossary-order-inner
     (doall
       (for [l (util/sort-by-locale (keys @(subscribe [::subs/data])))]
         ^{:key l}
         [:div.order-item
          {:on-click #(when (get @(subscribe [::subs/data]) l)
                        (dispatch-sync [:add-data [:active-letter] l])
                        (util/sleep (fn []
                                      (let [active-page (->> @(subscribe [::subs/active-page]) (util/sort-by-locale :title) first)]
                                        (select-content (:title active-page) (:id active-page))))
                                    150))
           :class    (when (= l active-letter) "is-active")
           :style    (when-not (get @(subscribe [::subs/data]) l) {:color "#d9dbe5"})}
          l]))]]])


(defn- titles-view [active-letter page]
  [:div
   [:img.big-letter {:src "./img/A.png"}]
   (doall
     (for [p (util/sort-by-locale :title page)]
       ^{:key (:id p)}
       [:a.terms-item
        {:on-click #(select-content (:title p) (:id p))
         :style    (when (= @(subscribe [::subs/active-content-id]) (:id p)) {:color "#2e81e8"})}
        (:title p)]))])


(defn- content-title-box-view [active-letter page active-content]
  [:div.glossary-terms
   [:div.glossary-terms-inner
    [:div.glossary-term-container
     [:div.term-image-a
      {:key (str "content-title-box-view-" active-letter)}]
     [titles-view active-letter page]]]])

(defn- build-tweet-text
  [title current-path]
  (str "https://twitter.com/intent/tweet"
       "?text=" (js/encodeURIComponent (str "Pisano Müşteri Deneyimi Sözlüğü: " title))
       "&url="  (js/encodeURIComponent current-path)
       "&via="  (js/encodeURIComponent "Pisano_TR")))

(defn- build-linkedin-post
  [title current-path]
  (str "https://www.linkedin.com/shareArticle?mini=true"
       "&url="     "https://sozluk.pisano.co"
       "&title="   (js/encodeURIComponent title)
       "&summary=" (js/encodeURIComponent "Pisano Müşteri Deneyimi Sözlüğü")))


(defn- render-social-share-buttons
  [title]
  (let [current-path (. (. js/document -location) -href)]
    [:div
     [:a.social-button 
      {:target "_blank" :href (str "http://www.facebook.com/sharer.php?u=" current-path)}
      [:img {:src "./img/facebook.png"}]]
     [:a.social-button
      {:target "_blank" :href (build-tweet-text title current-path)}
      [:img {:src "./img/twitter.png"}]]
     [:a.social-button
      {:target "_blank" :href (build-linkedin-post title current-path)}
      [:img {:src "./img/linkedin.png"}]]]))


(defn- navbar-view
  []
  [:div.navbar
   [:a.navbar-link {:target "_blank" :href "https://www.pisano.co/tr/tur"} "Platform Turu"]
   [:a.navbar-link {:target "_blank" :href "https://www.pisano.co/tr/incelemeler"} "Nasıl Çalışır"]
   [:a.navbar-link {:target "_blank" :href "https://www.pisano.co/tr/blog"} "Blog"]])


(defn- main-box-view [page active-content]
  [:<>
   [:div.glossary-term-entry
    [navbar-view]
    [:div.term-header
     [:div.term-title-image]]
    [:div.term-description
     [:div.container
      [:div.term-header-container
       [:h1.term-title [html-render (:title active-content)]]
       (render-social-share-buttons (:title active-content))]
      [html-render (:html active-content)]
      [html-render (:tag active-content)]]]]])


(defn- render-content-view
  [active-letter page active-content]
  [:<>
   [content-title-box-view active-letter page active-content]
   [main-box-view page active-content]])

(defn- welcome-screen-view
  []
  [:div.welcome-screen
   [:h1 {:style {:width "80%"}} "Müşteri Deneyimi Sözlüğü"]
   [:h3 {:style {:width "80%"}} "Müşteri Deneyimi Yönetimi'nde en sık kullanılan 200 terim ve açıklamaları"]])


(defn dashboard-view []
  (r/create-class
    {:component-will-mount #(do
                              (dispatch [::events/get-pages])
                              (let [href (some-> js/window .-location .-href)
                                    [hash title id] (take-last 3 (str/split href #"/"))]
                                (if (and (= "#" hash)
                                         (not (str/blank? title))
                                         (not (str/blank? id)))
                                  (dispatch [::events/find-and-set-content-by-id id]))))
     :reagent-render       (fn []
                             (let [page           @(subscribe [::subs/active-page])
                                   active-letter  @(subscribe [::subs/active-letter])
                                   active-content @(subscribe [::subs/active-content])]
                               [:div.glossary.glossary-entry.is-active
                                [vocabulary-box-view active-letter]
                                (if (nil? active-letter)
                                  [welcome-screen-view]
                                  (render-content-view active-letter page active-content))]))}))
