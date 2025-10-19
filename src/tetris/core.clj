(ns tetris.core
  (:require [quil.core :as q]
            [quil.middleware :as m]
            [tetris.game :as game]))

(def block-size-px 35)

(defn scale
  "Scale up a scalar used for grid positioning to the pixel scalar to use
   for rendering" 
  [scalar]
  (* scalar block-size-px))

(defn get-game-size []
  [(scale game/width) (scale game/height)])

(defn setup []
  ; Set frame rate to 30 frames per second.
  (q/frame-rate 30)
  ; Set color mode to HSB (HSV) instead of default RGB.
  (q/color-mode :hsb)
  ; setup function returns initial state. It contains
  ; circle color and position.
  {:color 0
   :angle 0})

(defn update-state [state]
  ; Update sketch state by changing circle color and position.
  {:color (mod (+ (:color state) 0.7) 255)
   :angle (+ (:angle state) 0.1)})

(defn draw-grid []
  (doall (for [i (range 1 game/width)]
    (q/line [(scale i) 0] [(scale i) (scale game/height)])))

  (doall (for [i (range 1 game/height)]
    (q/line [0 (scale i)] [(scale game/height) (scale i)]))))

(defn draw-state [state]
  ; Clear the sketch by filling it with light-grey color.
  (q/background 240)
  ; Set circle color.
  (q/fill (:color state) 255 255)
  ; Calculate x and y coordinates of the circle.
  (let [angle (:angle state)
        x (* 150 (q/cos angle))
        y (* 150 (q/sin angle))]
    ; Move origin point to the center of the sketch.
    (q/with-translation [(/ (q/width) 2)
                         (/ (q/height) 2)]
      ; Draw the circle.
      (q/ellipse x y 100 100)))
  ;; draw grid lines
  (when game/show-grid? (draw-grid)))


(q/defsketch tetris
  :title "You spin my circle right round"
  :size (get-game-size)
  ; setup function called only once, during sketch initialization.
  :setup setup
  ; update-state is called on each iteration before draw-state.
  :update update-state
  :draw draw-state
  :features [:keep-on-top]
  ; This sketch uses functional-mode middleware.
  ; Check quil wiki for more info about middlewares and particularly
  ; fun-mode.
  :middleware [m/fun-mode])

(defn -main
  "Entry point for the application"
  [& args]
  (println "Starting quil sketch")
  ;; sketch should auto start
  )
