(ns tetris.game-test
  (:require [clojure.test :refer [deftest is testing]]
            [tetris.game :as game])
  (:import [tetris.game TetronimoState]))

(def test-state (game/get-init-state))
(def test-keyboard-state {:key-pressed? false :key-as-keyword nil})

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

(def test-tetronimo (TetronimoState. :i :north 4 19))

(deftest test-update-state
  (testing "frozen-tetronimos"
    (is
     (= (->
         (game/update-state
          (assoc test-state :current-tetronimo test-tetronimo)
          test-keyboard-state)
         :frozen-tetronimos)
        [test-tetronimo])
     "should freeze current tetronimo when touching the ground")

    (is
     (= (->
         (game/update-state test-state test-keyboard-state)
         :frozen-tetronimos)
        [])
     "should not freeze tetronimos when current tetronimo is above the ground")

    (let [rotated-tetronimo (assoc test-tetronimo
                                   :orientation :east
                                   :y 18)]
      (is
       (= (->
           (game/update-state
            (assoc test-state :current-tetronimo rotated-tetronimo)
            test-keyboard-state)
           :frozen-tetronimos)
          [rotated-tetronimo])
       "should freeze rotated tetronimo when touching the ground"))))
