(ns pisano-cx-glossary.util
  (:require [ajax.core :as ajax]
            [clojure.string :as str]))

(def x->X {"ı" "I" 
           "i" "İ"
           "ç" "Ç"
           "ö" "Ö"
           "ü" "Ü"
           "ğ" "Ğ"
           "ş" "Ş"})


(defn upper-case
  [s]
  (when s
    (reduce-kv (fn [acc x X] (str/replace acc x X)) (str/upper-case s) x->X)))


(defn lower-case
  [s]
  (when s
    (reduce-kv (fn [acc x X] (str/replace acc X x)) (str/lower-case s) x->X)))


(defn create-custom-request-map
  [{:keys [method uri on-success on-failure] :or {on-success [:no-http-on-ok]
                                                  on-failure [:no-http-on-failure]}}]
  {:method          method
   :uri             uri
   :format          (ajax/json-request-format)
   :response-format (ajax/json-response-format {:keywords? true})
   :on-success      (if (vector? on-success) on-success [on-success])
   :on-failure      (if (vector? on-failure) on-failure [on-failure])})


(defn sleep
  [f ms]
  (js/setTimeout f ms))


(defn change-title!
  [title]
  (set! (.-title js/document) (str title " - Müşteri Deneyimi Sözlüğü")))


(defn sort-by-locale
  ([coll]
   (sort #(.localeCompare %1 %2 "tr") coll))
  ([k coll]
   (sort (fn [s1 s2]
           (let [active-lang "tr"
                 str1        (or (k s1) "")
                 str2        (or (k s2) "")]
             (.localeCompare str1 str2 active-lang)))
         coll)))


(defn push-state
  [uri]
  (.pushState (.-history js/window) "" "" uri))
