(ns sn.game
    (:require [reagent.core :as reagent :refer [atom]]))

(def boardsize 10)
(def interval 500)

(def init {:pos [5 5] :tail [[4 5] [3 5] [2 5]]
           :apple [2 2] :dir [1 0]
           :running false :reward 0 :score 0})

(defonce game (atom init))
(defonce timeout (atom nil))

(defonce lastaction (atom :fwd))

(defn move [state]
  (let [{:keys [pos dir tail]} state]
    (let [nextpos (mapv #(mod % boardsize) (map + pos dir))]
    (if (some #{nextpos} tail)
      (merge state {:running false :reward -1})
      (merge state
        (if (= nextpos (:apple state))
          {:pos nextpos :tail (conj tail pos)
           :apple [(rand-int 10) (rand-int 10)]
           :reward 1
           :score (inc (:score @game))}
          {:pos nextpos :tail (conj (butlast tail) pos) :reward 0}))))))

(defn step []
  (swap! game move))

(defn draw-point [ctx pos color]
  (set! (.-fillStyle ctx) color)
  (.beginPath ctx)
  (.rect ctx (* 30 (first pos)) (* 30 (last pos)) 30 30)
  (.fill ctx))


(defn draw! []
  (let [c (.getElementById js/document "c")
        ctx (.getContext c "2d")]
    (set! (.-fillStyle ctx) "rgb(255,255,255)")
    (.beginPath ctx)
    (.rect ctx 0 0 (* 30 boardsize) (* 30 boardsize))
    (.fill ctx)
    (.stroke ctx)
    (draw-point ctx (:apple @game) "rgb(255,0,0)")
    (draw-point ctx (:pos @game) "rgb(0,0,255)")
    (doall
      (map #(draw-point ctx % "rgb(0,255,255)") (:tail @game)))))

(defn key-handler [e]
  (swap! game assoc-in [:dir]
    (cond
      (= 37 (.-keyCode e)) [-1 0]
      (= 38 (.-keyCode e)) [0 -1]
      (= 39 (.-keyCode e)) [1 0]
      (= 40 (.-keyCode e)) [0 1]
      :else (:dir @game))))

(defn game-frame []
  (if (:running @game)
    (do
      (step)
      (draw!)
      (reset! timeout (js/setTimeout #(game-frame) interval)))))

(defn pause []
  (js/clearTimeout @timeout)
  (swap! game assoc-in [:running] (not (:running @game)))
  (game-frame))

(defn restart []
  (js/clearTimeout @timeout)
  (reset! game (assoc init :running true))
  (game-frame))

(defn matvec [m v]
  (map (fn [r] (apply + (map * r v)))  m))

(defn apple-vec []
  (map - (:apple @game) (:pos @game)))

(defn command [v]
  (swap!
    game
    assoc-in
      [:dir]
      (matvec
        (cond
          (= v :left) [[0 1] [-1 0]]
          (= v :right) [[0 -1] [1 0]]
          :else [[1 0] [0 1]])
        (:dir @game))))

(defn check-pos [pos]
  (some #{(map #(mod % boardsize) pos)} (:tail @game)))

(defn dir [v1 v2]
  (let [cross (apply - (map * (reverse v1) v2))]
    (cond
      (> cross 0.001) 1 ;right
      (< cross -0.001) -1 ;left
      :else 0)))

(defn state []
  [(not (not (check-pos (map + (:pos @game) (:dir @game)))))
   (not (not (check-pos (map + (:pos @game) (matvec [[0 -1] [1 0]] (:dir @game))))))
   (not (not (check-pos (map + (:pos @game) (matvec [[0 1] [-1 0]] (:dir @game))))))
   (dir (apple-vec) (:dir @game))
   @lastaction])


