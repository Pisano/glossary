(ns pisano-cx-glossary.util
  (:require [ajax.core :as ajax]))

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
