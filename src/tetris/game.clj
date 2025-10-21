(ns tetris.game)

; for development purposes
(def show-grid? true)

(def width 10)
(def height 20)

(def tetronimos {:i {:color [0 255 255]
                     :blocks [[-2 0] [-1 0] [0 0] [1 0]]}
                 :j {:color [0 0 255]
                     :blocks [[-1 -1] [-1 0] [0 0] [1 0]]}
                 :l {:color [255 162 0]
                     :blocks [[-1 0] [0 0] [1 0] [1 -1]]}
                 :o {:color [255 255 0]
                     :blocks [[0 0] [0 -1] [-1 0] [-1 -1]]}
                 :s {:color [0 230 30]
                     :blocks [[-1 0] [0 0] [0 -1] [1 -1]]}
                 :t {:color [188 0 255]
                     :blocks [[-1 0] [0 0] [0 -1] [1 0]]}
                 :z {:color [255 0 0]
                     :blocks [[-1 -1] [0 -1] [0 0] [1 0]]}
                 ; why does VS code indent this so far in
                 })

(defrecord State [current-tetronimo
                  level
                  time-since-last-move
                  key-pressed?])

(defrecord TetronimoState [key orientation x y])

(defn get-init-state []
  (State. (TetronimoState. :z :north 4 0) 0 0 false))

(defn rotate-key-pressed? [last-state keyboard-state]
  (and
   (:key-pressed? keyboard-state)
   (not= (:key-pressed? keyboard-state) (:key-pressed? last-state))
   (or (some #(= % (:key-as-keyword keyboard-state)) [:up :w]) false)))

(defn update-state [state keyboard-state]
  (-> state
      (assoc :key-pressed? (:key-pressed? keyboard-state))
      (update-in [:time-since-last-move] #(mod (inc %) 30))
      (update-in [:current-tetronimo :y]
                 (if (= (:time-since-last-move state) 0) inc identity))
      (update-in [:current-tetronimo :orientation]
                 #(if (rotate-key-pressed? state keyboard-state)
                    (case %
                      :north :east
                      :east :south
                      :south :west
                      :west :north)
                    %))))

(defn rotate-blocks
  "rotate the blocks such that they are pointing in one of four directions:
   north, south, east, west"
  [blocks orientation]
  (map (fn [[x y]]
         (case orientation
           :north [x y]
           :south [(- x) (- y)]
           :east [(- y) x]
           :west [y (- x)])) blocks))
