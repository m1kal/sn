(ns sn.ai
    (:require [reagent.core :as reagent :refer [atom]]
               [sn.game :as game
                :refer
                [game timeout draw! pause restart
                 state command step]]))


(defonce qtable (atom {}))
(def alpha (atom 0.1))
(def gamma 0.1)
(def actions [:fwd :left :right])
(def newaction {:fwd 1 :left 0.5 :right 0.5})
(def threshold (atom 0.8))


;deterministic action
(defn action []
  (let [s (state)
        cmd (cond
              (and (s 0) (s 1)) :left
              (and (s 0) (s 2)) :right
              (and (s 1) (s 2)) :fwd
              (and (not (s 2)) (= (s 3) -1)) :left
              (and (not (s 1)) (= (s 3) 1)) :right
              (s 0) :right
              :else :fwd)]
    (command cmd)
    (step)))

(defn run [steps]
  (if (or (zero? steps) (= -1 (:reward @game)))
    @game
    (do
      (action)
      (draw!)
      (js/setTimeout #(run (dec steps)) 50))))


;state-based q-learning
(defn qtable-get [state]
  (or (@qtable state) newaction))

(defn take-action [state]
  (if (> (rand) @threshold)
    (rand-nth actions)
    (first (last (sort-by last (qtable-get state))))))

(defn q-step [update]
  (let [
        s0 (state)
        a (take-action s0)
        q ((qtable-get s0) a)
        g (command a)
        x (step)
        y (reset! game/lastaction a)
        reward (:reward @game)
        s1 (state)
        qmax (apply max (vals (qtable-get s1)))
        qnew (+ (* q (- 1 @alpha)) (* @alpha (+ reward (* gamma qmax))))
        qtablenew (assoc (qtable-get s0) a qnew)
        ]
    (if update (swap! qtable assoc s0 qtablenew))))

(defn play [steps learn]
  (if (or (zero? steps) (= -1 (:reward @game)))
    (:score @game)
    (let [thr @threshold]
      (if learn (reset! @threshold 1))
      (q-step learn)
      (reset! threshold thr)
      (draw!)
      (reset!
        timeout
        (js/setTimeout #(play (dec steps) learn) 50)))))

(defn learn-silent [steps]
  (loop [x steps]
    (if (or (zero? x) (= -1 (:reward @game)))
      (draw!)
      (do
        (q-step true)
        (recur (dec x))))))

(defn stats [values]
  [(apply max values) (/ (apply + values) (count values))])

(defn train [steps]
  (loop [x steps scores []]
    (restart)
    (pause)
    (learn-silent 1000)
    (swap! threshold #(- 1 (* 0.999 (- 1 %))))
    (swap! alpha #(* 0.999 %))
    (if (zero? x)
      (stats scores)
      (recur (dec x) (conj scores (:score @game)) ))))

(defn untrain []
  (reset! qtable {})
  (reset! threshold 0.5)
  (reset! alpha 0.2)
)
