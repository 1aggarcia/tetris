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
  ; Set FPS
  (q/frame-rate 2)
  ; setup function returns initial state.
  {:orientation :east})

(defn update-state [state]
  {:orientation (case (:orientation state)
                  :north :east
                  :east :south
                  :south :west
                  :west :north)})

(defn draw-grid []
  (doall (for [i (range 1 game/width)]
           (q/line [(scale i) 0] [(scale i) (scale game/height)])))

  (doall (for [i (range 1 game/height)]
           (q/line [0 (scale i)] [(scale game/height) (scale i)]))))

(defn draw-tetronimo
  "draw a tetronimo based on the orientation and center position passed in"
  [state, tetronimo, [center-x, center-y]]
  (let [rotated-blocks (game/rotate-blocks (:blocks tetronimo) (:orientation state))]
    (apply q/fill (:color tetronimo))
    (doall (for [[x-offset y-offset] rotated-blocks]
             (q/rect
              (+ (scale center-x) (scale x-offset))
              (+ (scale center-y) (scale y-offset))
              block-size-px
              block-size-px)))))

(defn draw-state [state]
  ; Clear the sketch by filling it with light-grey color.
  (q/background 240)
  ;; draw grid lines
  (when game/show-grid? (draw-grid))

  (draw-tetronimo state (:i game/tetronimos) [3, 3])
  (draw-tetronimo state (:j game/tetronimos) [3, 6])
  (draw-tetronimo state (:l game/tetronimos) [3, 9])
  (draw-tetronimo state (:o game/tetronimos) [3, 12])
  (draw-tetronimo state (:s game/tetronimos) [3, 15])
  (draw-tetronimo state (:t game/tetronimos) [3, 18])
  (draw-tetronimo state (:z game/tetronimos) [7, 2]))


(q/defsketch tetris
  :title "Tetronimos Test"
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
