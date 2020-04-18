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
  [slug id]
  (dispatch [::events/find-and-set-content-by-id id])
  (change-uri! slug id))

(defn- letters-view
  [active-letter]
  [:div.letters
   [:a.logo
    {:href "/"}
    [:img {:src "img/logo.png"}]]
   [:div.letters-container
    (doall
      (for [l (util/sort-by-locale (keys @(subscribe [::subs/data])))]
        ^{:key l}
        [:div.letter
         {:on-click #(when (get @(subscribe [::subs/data]) l)
                       (dispatch-sync [:add-data [:active-letter] l])
                       (util/sleep (fn []
                                     (let [active-page (->> @(subscribe [::subs/active-page]) (util/sort-by-locale :title) first)]
                                       (select-content (:slug active-page) (:id active-page))))
                                   150))
          :class    (when (= l active-letter) "is-active")
          :style    (when-not (get @(subscribe [::subs/data]) l) {:color "#d9dbe5"})}
         l]))]])

(defn- content-title-box-view
  [active-letter page]
  [:div.posts
   [:div.big-letter-container
    [:img.big-letter {:src (str "./img/letters/" active-letter ".png")}]]
   (doall
     (for [p (util/sort-by-locale :title page)]
       ^{:key (:id p)}
       [:div.post
        {:on-click #(select-content (:slug p) (:id p))
         :style    (when (= @(subscribe [::subs/active-content-id]) (:id p)) {:color "#2e81e8"})}
        (:title p)]))])

(defn- build-tweet-text
  [title current-path]
  (str "https://twitter.com/intent/tweet"
       "?text=" (js/encodeURIComponent (str title " Nedir? | Pisano Müşteri Deneyimi Sözlüğü"))
       "&url="  (js/encodeURIComponent current-path)
       "&via="  (js/encodeURIComponent "Pisano_TR")))

(defn- build-linkedin-post
  [title current-path]
  (str "https://www.linkedin.com/shareArticle?mini=true"
       "&url="     (js/encodeURIComponent current-path)
       "&title="   (js/encodeURIComponent (str title " Nedir? | Pisano Müşteri Deneyimi Sözlüğü"))
       "&summary=" (js/encodeURIComponent "Pisano Müşteri Deneyimi Sözlüğü")))

(defn- render-social-share-buttons
  [title]
  (let [current-path (.-href (.-location js/document))]
    [:div
     [:a.social-button 
      {:target "_blank" :href (str "http://www.facebook.com/sharer.php?u=" (js/encodeURIComponent current-path))}
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
   [:a.navbar-link {:target "_blank" :href "https://www.pisano.co/tr"} "Pisano"]
   [:a.navbar-link {:target "_blank" :href "https://www.pisano.co/tr/tur"} "Platform Turu"]
   [:a.navbar-link {:target "_blank" :href "https://www.pisano.co/tr/incelemeler"} "Nasıl Çalışır"]
   [:a.navbar-link {:target "_blank" :href "https://www.pisano.co/tr/blog"} "Blog"]])

(defn- main-box-view [page active-content]
  [:<>
   [:div.content
    [navbar-view]
    [:div.header-image]
    [:div.container
     [:div.term-header-container
      [:h1.term-title [html-render (:title active-content)]]
      (render-social-share-buttons (:title active-content))]
     [:div.ghost-content
      [html-render (:html active-content)]
      [html-render (:tag active-content)]]]]])

(defn- render-content-view
  [active-letter page active-content]
  [:<>
   [content-title-box-view active-letter page]
   [main-box-view page active-content]])

(defn- welcome-screen-view
  []
  [:div.welcome-screen
   [:h1 {:style {:width "80%"}} "Müşteri Deneyimi Sözlüğü"]
   [:h3 {:style {:width "80%"}} "Müşteri Deneyimi Yönetimi'nde en sık kullanılan 200 terim ve açıklamaları"]])

(defn dispatch-by-uri
  []
  (let [href (some-> js/window .-location .-href)
        [hash title id] (take-last 3 (str/split href #"/"))]
    (if (and (= "#" hash)
             (not (str/blank? title))
             (not (str/blank? id)))
      (dispatch [::events/find-and-set-content-by-id id]))))

(defn dashboard-view []
  (r/create-class
    {:component-will-mount #(do
                              (dispatch [::events/get-pages])
                              (dispatch-by-uri))
     :component-did-mount (fn []
                            ;; Doing this manually, CLJS version has some odd bug with secretary.
                            (.addEventListener js/window "hashchange" #(dispatch-by-uri)))
     :reagent-render       (fn []
                             (let [page           @(subscribe [::subs/active-page])
                                   active-letter  @(subscribe [::subs/active-letter])
                                   active-content @(subscribe [::subs/active-content])]
                               [:<>
                                [letters-view active-letter]
                                (if (nil? active-letter)
                                  [welcome-screen-view]
                                  (render-content-view active-letter page active-content))]))}))
