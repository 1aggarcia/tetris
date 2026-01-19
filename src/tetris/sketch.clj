(ns tetris.sketch
  "I/O wrapper for reading and updating game state"
  (:require [quil.core :as q]
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
  (q/frame-rate 30)
  ; setup function returns initial state.
  (game/get-init-state))

(defn get-keyboard-state
  "Return current keyboard state as a map.
   Only works inside of sketch functions."
  []
  {:key-pressed? (q/key-pressed?) :key-as-keyword (q/key-as-keyword)})

(defn update-state [state]
  (game/update-state state (get-keyboard-state) (game/generate-random-seed)))

(defn draw-grid []
  (doall (for [i (range 1 game/width)]
           (q/line [(scale i) 0] [(scale i) (scale game/height)])))

  (doall (for [i (range 1 game/height)]
           (q/line [0 (scale i)] [(scale game/height) (scale i)]))))

(defn draw-state-text [state]
  (do
    (q/fill 0 0 0)
    (q/text-size 13)
    (q/text (game/state-to-string state) 10 23)))

(defn draw-tetronimo
  "draw a tetronimo based on the orientation and center position passed in"
  [tetronimo-state]
  (let [tetronimo ((:key tetronimo-state) game/tetronimos)
        orientation (:orientation tetronimo-state)
        x (:x tetronimo-state)
        y (:y tetronimo-state)
        rotated-blocks (game/rotate-blocks (:blocks tetronimo) orientation)]
    (apply q/fill (:color tetronimo))
    (doall (for [[x-offset y-offset] rotated-blocks]
             (q/rect
              (+ (scale x) (scale x-offset))
              (+ (scale y) (scale y-offset))
              block-size-px
              block-size-px)))))

(defn draw-state [state]
  ; Clear the sketch by filling it with light-grey color.
  (q/background 240)
  ;; draw grid lines
  (when game/show-grid? (draw-grid))

  (doall (for [tetronimo (:frozen-tetronimos state)]
           (draw-tetronimo tetronimo)))
  (draw-tetronimo (:current-tetronimo state))
  (when game/show-state? (draw-state-text state)))
