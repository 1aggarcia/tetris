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
