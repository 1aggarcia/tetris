# Data Models 

## Game State

- Collection of frozen tetronimos
    - X positions
    - Y positions
- Tetronimo currently in motion
    - Type (enum)
    - Orientation (enum: 0, 90, 180, 270)
    - X position
    - Y position
- Next N tetronimos (should be able to configure N)
- Current Level
- Frames until next move

## Enums
- Tetronimos
    - I
    - J
    - L
    - O
    - S
    - T
    - Z

## Screens
- Welcome screen
- Gameplay screen
    - Grid (20 x 10) on the left
    - Next tetronimos top-right
    - Score and level on bottom-right
- Game over screen
