(ns tetris.game-test
  (:require [clojure.test :refer [deftest is testing are]]
            [tetris.game :as game]
            [clojure.string :as string])
  (:import [tetris.game TetrominoState]))

; TODO: use humane-test-output to get more readable test failure messages

(def test-state (game/get-init-state))
(def test-keyboard-state {:key-pressed? false :key-as-keyword nil})
(def test-tetromino (TetrominoState. :i :north 4 19))

(deftest test-create-new-tetromino
  (is (=
       ; generate a tetromino with all random seeds in range 7000
       ; count up the frequency of each type and compare to reference
       ; should be deterministic based on the seed
       (->> (range 7000)
            (map #(game/create-new-tetromino %))
            (map #(:key %))
            frequencies)
       {:t 1002, :s 1003, :o 1000, :l 998, :j 998, :i 1000, :z 999})
      "should generate all tetromino types with equal probability")

  (is (every? #(= % (TetrominoState. nil :north 4 0))
              (->> (range 7000)
                   (map game/create-new-tetromino)
                   (map #(assoc % :key nil)))) ; key is random so don't compare it
      "should set all properties besides key the same regardless of random seed"))

(deftest test-get-min-tetromino-x
  (are [key orientation expected]
       (= expected
          (game/get-min-tetromino-x
           (TetrominoState. key orientation 5 0)))
    :i :north 3
    :j :north 4
    :l :south 4
    :o :south 5
    :s :east 5
    :t :east 5
    :z :west 4))

(deftest test-get-max-tetromino-x
  (are [key orientation expected]
       (= expected
          (game/get-max-tetromino-x
           (TetrominoState. key orientation 5 0)))
    :i :north 6
    :j :north 6
    :l :south 6
    :o :south 6
    :s :east 6
    :t :east 6
    :z :west 5))

(deftest test-state-to-string
  (let [expected ["{:current-tetromino {:key :i, :orientation :north, :x 4, :y 19},"
                  " :frozen-blocks {},"
                  " :level 0,"
                  " :time-since-last-move 0,"
                  " :key-pressed? false}"]
        actual (game/state-to-string (assoc test-state :current-tetromino test-tetromino))]
    (is (= expected (clojure.string/split-lines actual)))))

(deftest test-rotate-key-pressed
  (is
   (true? (game/rotate-key-pressed?
           test-state
           {:key-pressed? true :key-as-keyword :up}))
   "should return true when up arrow is pressed")

  (is
   (true? (game/rotate-key-pressed?
           test-state
           {:key-pressed? true :key-as-keyword :w}))
   "should return true when W is pressed")

  (is
   (false? (game/rotate-key-pressed?
            test-state
            {:key-pressed? true :key-as-keyword :a}))
   "should return false when unrecognized key is pressed")

  (is
   (false? (game/rotate-key-pressed?
            test-state
            {:key-pressed? false}))
   "should return false when no key is pressed")

  (is
   (false? (game/rotate-key-pressed?
            (assoc test-state :key-pressed? true)
            {:key-pressed? true :key-as-keyword :up}))
   "should return false when key was already pressed"))

(deftest test-update-current-tetromino
  (testing "should update X based on key pressed"
    (let [test-tetromino (:current-tetromino test-state)]
      (are [test-key expected-x]
           (= (assoc test-tetromino :x expected-x :y 1)
              (game/update-current-tetromino
               test-state
               {:key-pressed? true :key-as-keyword test-key}
               test-keyboard-state))
        :left 3
        :a 3
        :right 5
        :d 5))))

(deftest test-get-blocks
  (let [input (TetrominoState. :s :north 0 0)
        expected [[-1 0] [0 0] [0 -1] [1 -1]]
        actual (game/get-blocks input)]
    (is (= expected actual) "returns correct blocks without offset"))

  (let [input (TetrominoState. :s :north 1 2)
        expected [[0 2] [1 2] [1 1] [2 1]]
        actual (game/get-blocks input)]
    (is (= expected actual) "returns correct blocks with offset"))

  (let [input (TetrominoState. :s :east 0 0)
        expected [[0 -1] [0 0] [1 0] [1 1]]
        actual (game/get-blocks input)]
    (is (= expected actual) "returns correct blocks with rotation")))

(deftest test-block-colliding-bottom
  (are [block-delta block]
       (let [frozen-blocks {[5 5] :i}]
         (true? (game/block-colliding? frozen-blocks block-delta block)))
    [0 1] [5 4] ; above frozen block
    [-1 0] [6 5] ; left of frozen block
    [1 0] [4 5] ; right of frozen block
    [0 1] [5 (dec game/height)]) ; just above the ground

  (is
   (false? (game/block-colliding? {[5 5] :i} [0 1] [5 3]))
   "should not detect block outside collision bounds"))

(deftest test-can-move-left
  (is
   (true? (game/can-move-left? test-state))
   "not touching left wall")

  (is
   (false? (let [test-tetromino (TetrominoState. :i :west 0 0)
                 test-state-blocked (assoc test-state :current-tetromino test-tetromino)]
             (game/can-move-left? test-state-blocked)))
   "touching left wall"))

(deftest test-update-state
  (testing "current-tetromino"
    (let [input (:current-tetromino test-state)
          expected (update-in input [:y] inc)]
      (is
       (=
        (-> (game/update-state test-state test-keyboard-state 0)
            :current-tetromino)
        expected)
       "should move tetromino if time-since-last-move is 0"))

    (let [expected (:current-tetromino test-state)]
      (is
       (=
        (-> (assoc test-state :time-since-last-move 1)
            (game/update-state test-keyboard-state 0)
            :current-tetromino)
        expected)
       "should not move tetromino if time-since-last-move is not 0"))

    (let [keyboard-state {:key-pressed? true :key-as-keyword :up}
          input (:current-tetromino test-state)
          expected (assoc input :orientation :east :y 1)]
      (is
       (=
        (-> (game/update-state test-state keyboard-state 0)
            :current-tetromino)
        expected)
       "should rotate tetromino if rotate key is pressed"))

    (let [input-state (assoc test-state :current-tetromino test-tetromino)
          expected (TetrominoState. :t :north 4 0)]
      (is
       (=
        (-> (game/update-state input-state test-keyboard-state 0)
            :current-tetromino)
        expected)
       "should replace tetromino when touching the ground")))

  (testing "frozen-blocks"
    (is
     (= (->
         (game/update-state
          (assoc test-state :current-tetromino test-tetromino)
          test-keyboard-state
          0)
         :frozen-blocks)
        {[2 19] :i [3 19] :i [4 19] :i [5 19] :i})
     "should freeze current tetromino when touching the ground")

    (is
     (= (->
         (game/update-state test-state test-keyboard-state 0)
         :frozen-blocks)
        {})
     "should not freeze tetrominos when current tetromino is above the ground")

    (let [rotated-tetromino (assoc test-tetromino
                                   :orientation :east
                                   :y 18)]
      (is
       (= (->
           (game/update-state
            (assoc test-state :current-tetromino rotated-tetromino)
            test-keyboard-state
            0)
           :frozen-blocks)
          {[4 16] :i [4 17] :i [4 18] :i [4 19] :i})
       "should freeze rotated tetromino when touching the ground"))))
