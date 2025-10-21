(ns tetris.game-test
  (:require [clojure.test :refer [deftest is]]
            [tetris.game :as game]))

(def test-state (game/get-init-state))

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
 