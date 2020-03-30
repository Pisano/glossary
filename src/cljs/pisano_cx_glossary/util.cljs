(ns pisano-cx-glossary.util
  (:require [ajax.core :as ajax]
            [clojure.string :as str]))

(defn create-custom-request-map
  ([type uri]
   (create-custom-request-map type uri nil nil))
  ([type uri on-success]
   (create-custom-request-map type uri on-success nil))
  ([type uri on-success on-fail]
   (cond-> {:method          type
            :uri             uri
            :format          (ajax/json-request-format)
            :response-format (ajax/json-response-format {:keywords? true})
            :on-success      (if (vector? on-success) on-success [on-success])
            :on-failure      (if (vector? on-fail) on-fail [on-fail])}
           (nil? on-success) (assoc :on-success [:no-http-on-ok])
           (nil? on-fail) (assoc :on-failure [:no-http-on-failure]))))


(defn upper-case
  [s]
  (some-> s
          (str/replace #"ı" "I")
          (str/replace #"i" "İ")
          (str/replace #"ç" "Ç")
          (str/replace #"ö" "Ö")
          (str/replace #"ü" "Ü")
          (str/replace #"ğ" "Ğ")
          (str/replace #"ş" "Ş")
          (str/upper-case)))


(defn lower-case
  [s]
  (some-> s
      (str/replace #"I" "ı")
      (str/replace #"İ" "i")
      (str/replace #"Ç" "ç")
      (str/replace #"Ö" "ö")
      (str/replace #"Ü" "ü")
      (str/replace #"Ğ" "ğ")
      (str/replace #"Ş" "ş")
      (str/lower-case)))


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