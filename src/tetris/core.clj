(ns tetris.core
  (:require [quil.core :as q]
            [quil.middleware :as m]
            [tetris.sketch :as sketch]))

(q/defsketch tetris
  :title "Tetrominos Test"
  :size (sketch/get-game-size)
  ; setup function called only once, during sketch initialization.
  :setup sketch/setup
  ; update-state is called on each iteration before draw-state.
  :update sketch/update-state
  :draw sketch/draw-state
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
