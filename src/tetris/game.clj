(ns tetris.game
  "Game logic and configuration decoupled from I/O"
  (:require [clojure.pprint :as pp]))

; for development purposes
(def show-grid? true)
(def show-state? true)

(def width 10)
(def height 20)

(def tetrominos {:i {:color [0 255 255]
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
                     :blocks [[-1 -1] [0 -1] [0 0] [1 0]]}})
                 ; why does VS code indent this so far in

(def tetromino-keys (keys tetrominos))

(defrecord State [current-tetromino
                  frozen-blocks
                  level
                  time-since-last-move
                  key-pressed?])

(defn state-to-string
  "converts the state passed in to a readable string"
  [state]
  (with-out-str
    (binding [pp/*print-right-margin* 70]
      (pp/pprint state))))

(defrecord TetrominoState [key orientation x y])

(defn generate-random-seed []
  (rand-int Integer/MAX_VALUE))

(defn create-new-tetromino [random-seed]
  (let [rand (java.util.Random. random-seed)
        rand-idx (.nextInt rand (count tetromino-keys))
        rand-key (nth tetromino-keys rand-idx)]
    (TetrominoState. rand-key :north 4 0)))

(defn get-init-state []
  (State. (create-new-tetromino (generate-random-seed)) {} 0 0 false))

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

(defn get-blocks
  "Calculate the blocks occupied by a tetromino from the tetromino state"
  [{:keys [x y key orientation]}]
  (as-> (get-in tetrominos [key :blocks]) $
    (rotate-blocks $ orientation)
    (map (fn [[bx by]] [(+ bx x) (+ by y)]) $)))

(defn one-of-keys-pressed
  "Return a function that determines if one of `keys` is pressed based on
   the state passed in."
  [keys]
  (fn [last-state keyboard-state]
    (and
     (:key-pressed? keyboard-state)
     (not= (:key-pressed? keyboard-state) (:key-pressed? last-state))
     (or (some #(= % (:key-as-keyword keyboard-state)) keys) false))))

(def rotate-key-pressed? (one-of-keys-pressed [:up :w]))

(def left-key-pressed? (one-of-keys-pressed [:left :a]))

(def right-key-pressed? (one-of-keys-pressed [:right :d]))

(defn block-colliding?
  "Determine if the block passed is colliding with one of the frozen blocks.
   
   - `frozen-blocks`: map of `[x y] -> key`, representing frozen blocks on the grid
   - `[dx dy]`: change in X and Y to apply to the block to determine if there is a
     collision in that direction
   - `[x y]`: the block to test collision against"
  [frozen-blocks [dx dy] [x y]]
  (let [new-x (+ x dx)
        new-y (+ y dy)]
    (or
     ; TODO: detect collision on top
     (< new-x 0)
     (>= new-x width)
     (>= new-y height)
     (contains? frozen-blocks [new-x new-y]))))

(defn tetromino-colliding?
  "Determine if the current tetromino in state is colliding with one of the
   frozen tetrominos.
   
   - `[dx dy]`: change in X and Y to apply to the tetromino to determine if there
     is a collision in that direction"
  [{:keys [current-tetromino frozen-blocks]} [dx dy]]
  (->> (get-blocks current-tetromino)
       (some (partial block-colliding? frozen-blocks [dx dy]))
       (boolean)))

(defn can-move-left?
  [last-state]
  (not (tetromino-colliding? last-state [-1 0])))

(defn can-move-right?
  [last-state]
  (not (tetromino-colliding? last-state [1 0])))

(defn colliding-bottom?
  [last-state]
  (tetromino-colliding? last-state [0 1]))

(defn update-frozen-blocks
  "Copy all blocks from the current tetromino to the frozen blocks if colliding"
  [{:keys [current-tetromino frozen-blocks] :as last-state}]
  ; TODO: allow grace period for blocks to move even if colliding on the bottom
  (if (colliding-bottom? last-state)
    (let [key (:key current-tetromino)
          blocks (get-blocks current-tetromino)]
      (reduce #(assoc %1 %2 key) frozen-blocks blocks))
    frozen-blocks))

(defn update-tetromino-x [last-x state keyboard-state]
  (cond
    (and
     (left-key-pressed? state keyboard-state)
     (can-move-left? state))
    (dec last-x)
    (and
     (right-key-pressed? state keyboard-state)
     (can-move-right? state))
    (inc last-x)
    :else last-x))

(defn update-current-tetromino
  "
   - Move the tetromino down if it is time to do so
   - Move the tetromino according to the keys pressed
   - Spawn a new tetromino if the current one touches the ground
   "
  [state keyboard-state random-seed]
  (if (colliding-bottom? state)
    ; TODO: Game over instead if the block is also colliding on top
    ; Right now this will spawn new blocks on every frame if it collides on top
    (create-new-tetromino random-seed)
    (let [tetromino (:current-tetromino state)]
      (-> tetromino
          (update-in [:y]
                     (if (= (:time-since-last-move state) 0) inc identity))

          (update-in [:x]
                     #(update-tetromino-x % state keyboard-state))

          ;; TODO: account for rotating while colliding with something
          (update-in [:orientation]
                     #(if (rotate-key-pressed? state keyboard-state)
                        (case %
                          :north :east
                          :east :south
                          :south :west
                          :west :north)
                        %))))))

(defn update-state [state keyboard-state random-seed]
  (-> state
      (assoc :key-pressed? (:key-pressed? keyboard-state))
      (update-in [:time-since-last-move] #(mod (inc %) 5))
      (assoc
       :frozen-blocks (update-frozen-blocks state)
       :current-tetromino (update-current-tetromino
                           state
                           keyboard-state
                           random-seed))))
