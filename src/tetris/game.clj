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
                     :blocks [[-1 -1] [0 -1] [0 0] [1 0]]}})
                 ; why does VS code indent this so far in

(defrecord State [current-tetronimo
                  frozen-tetronimos
                  level
                  time-since-last-move
                  key-pressed?])

(defrecord TetronimoState [key orientation x y])

(defn get-init-state []
  (State. (TetronimoState. :z :north 4 0) [] 0 0 false))

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

(defn get-max-tetronimo-y
  "return the largest Y position occupied by the tetronimo passed in"
  [tetronimo-state]
  (let [tetronimo ((:key tetronimo-state) tetronimos)
        rotated (rotate-blocks (:blocks tetronimo) (:orientation tetronimo-state))]
    (->> rotated
         (map (fn [[_ y]] y))
         (apply max)
         (+ (:y tetronimo-state)))))

(defn rotate-key-pressed? [last-state keyboard-state]
  (and
   (:key-pressed? keyboard-state)
   (not= (:key-pressed? keyboard-state) (:key-pressed? last-state))
   (or (some #(= % (:key-as-keyword keyboard-state)) [:up :w]) false)))

(defn current-tetronimo-touching-ground?
  [last-state]
  (>=
   (get-max-tetronimo-y (:current-tetronimo last-state))
   (dec height)))

(defn update-frozen-tetronimos
  "Copy the current tetronimo to the frozen tetronimos if touching the ground"
  [last-state]
  (if (current-tetronimo-touching-ground? last-state)
    (conj (:frozen-tetronimos last-state) (:current-tetronimo last-state))
    (:frozen-tetronimos last-state)))

(defn update-current-tetronimo
  "
   - Move the tetronimo down if it is time to do so
   - Rotate the tetronimo if the rotate key is pressed
   - Spawn a new tetronimo if the current one touches the ground (TODO)
   "
  [state keyboard-state]
  (let [tetronimo (:current-tetronimo state)]
    (-> tetronimo
        (update-in [:y]
                   (if (= (:time-since-last-move state) 0) inc identity))

        (update-in [:orientation]
                   #(if (rotate-key-pressed? state keyboard-state)
                      (case %
                        :north :east
                        :east :south
                        :south :west
                        :west :north)
                      %)))))

(defn update-state [state keyboard-state]
  (-> state
      (assoc :key-pressed? (:key-pressed? keyboard-state))
      (update-in [:time-since-last-move] #(mod (inc %) 30))
      (assoc
       :frozen-tetronimos (update-frozen-tetronimos state)
       :current-tetronimo (update-current-tetronimo state keyboard-state))))
