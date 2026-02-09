(ns tetris.game
  "Game logic and configuration decoupled from I/O"
  (:require [clojure.pprint :as pp]))

; for development purposes
(def show-grid? true)
(def show-state? true)

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
                     :blocks [[-1 -1] [0 -1] [0 0] [1 0]]}})
                 ; why does VS code indent this so far in

(def tetronimo-keys (keys tetronimos))

(defrecord State [current-tetronimo
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

(defrecord TetronimoState [key orientation x y])

(defn generate-random-seed []
  (rand-int Integer/MAX_VALUE))

(defn create-new-tetronimo [random-seed]
  (let [rand (java.util.Random. random-seed)
        rand-idx (.nextInt rand (count tetronimo-keys))
        rand-key (nth tetronimo-keys rand-idx)]
    (TetronimoState. rand-key :north 4 0)))

(defn get-init-state []
  (State. (create-new-tetronimo (generate-random-seed)) {} 0 0 false))

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

(defn get-min-tetronimo-x
  "return the smallest X position occupied by the tetronimo passed in"
  [tetronimo-state]
  (let [tetronimo ((:key tetronimo-state) tetronimos)
        rotated (rotate-blocks (:blocks tetronimo) (:orientation tetronimo-state))]
    (->> rotated
         (map (fn [[x _]] x))
         (apply min)
         (+ (:x tetronimo-state)))))

(defn get-max-tetronimo-x
  "return the largest X position occupied by the tetronimo passed in"
  [tetronimo-state]
  (let [tetronimo ((:key tetronimo-state) tetronimos)
        rotated (rotate-blocks (:blocks tetronimo) (:orientation tetronimo-state))]
    (->> rotated
         (map (fn [[x _]] x))
         (apply max)
         (+ (:x tetronimo-state)))))

(defn get-blocks
  "Calculate the blocks occupied by a tetronimo from the tetronimo state"
  [{:keys [x y key orientation]}]
  (as-> (get-in tetronimos [key :blocks]) $
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

;; TODO: check for collision with other tetronimos
(defn can-move-left? [last-state]
  (>
   (get-min-tetronimo-x (:current-tetronimo last-state))
   0))

;; TODO: check for collision with other tetronimos
(defn can-move-right? [last-state]
  (<
   (get-max-tetronimo-x (:current-tetronimo last-state))
   (dec width)))

(defn block-colliding-bottom?
  [frozen-blocks [x y]]
  (let [y-below (inc y)]
    (or
     (>= y-below height)
     (contains? frozen-blocks [x y-below]))))

(defn colliding-bottom?
  [{:keys [current-tetronimo frozen-blocks]}]
  (->> (get-blocks current-tetronimo)
       (some (partial block-colliding-bottom? frozen-blocks))
       (boolean)))

(defn update-frozen-blocks
  "Copy all blocks from the current tetronimo to the frozen blocks if colliding"
  [{:keys [current-tetronimo frozen-blocks] :as last-state}]
  (if (colliding-bottom? last-state)
    (let [key (:key current-tetronimo)
          blocks (get-blocks current-tetronimo)]
      (reduce #(assoc %1 %2 key) frozen-blocks blocks))
    frozen-blocks))

(defn update-tetronimo-x [last-x state keyboard-state]
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

(defn update-current-tetronimo
  "
   - Move the tetronimo down if it is time to do so
   - Move the tetronimo according to the keys pressed
   - Spawn a new tetronimo if the current one touches the ground
   "
  [state keyboard-state random-seed]
  (if (colliding-bottom? state)
    ; TODO: Game over instead if the block is also colliding on top
    ; Right now this will spawn new blocks on every frame if it collides on top
    (create-new-tetronimo random-seed)
    (let [tetronimo (:current-tetronimo state)]
      (-> tetronimo
          (update-in [:y]
                     (if (= (:time-since-last-move state) 0) inc identity))

          (update-in [:x]
                     #(update-tetronimo-x % state keyboard-state))

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
       :current-tetronimo (update-current-tetronimo
                           state
                           keyboard-state
                           random-seed))))
