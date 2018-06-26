(ns sn.core
    (:require [reagent.core :as reagent :refer [atom]]
              [sn.game :as game
                :refer
                [game timeout draw! pause restart
                 state command step boardsize interval
                 key-handler]]
              [sn.ai :as ai
               :refer
               [train play run
                 untrain]]))

(enable-console-print!)

(defn app-main []
  [:div
   [:h1 "Snake"]
   [:canvas {:width (* 30 boardsize) :height (* 30 boardsize) :id "c"}]
   [:div
     [:input
      {:type "button" :value "pause" :on-click pause}]
     [:input
      {:type "button" :value "start" :on-click restart}]
     "score: " (:score @game)]
   [:div
     [:input
      {:type "button" :value "train" :on-click #(train 100)}]
     [:input
      {:type "button" :value "untrain" :on-click untrain}]
     [:input
      {:type "button" :value "play" :on-click #(play 1000 false)}]
     [:input
      {:type "button" :value "algorithm" :on-click #(run 1000)}]]])

(.addEventListener js/window "keydown" key-handler)

(reagent/render-component [app-main]
                          (. js/document (getElementById "app")))
