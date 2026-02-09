# Data Models 

## Game State

- Map of frozen blocks
    - Key: [x, y] position
    - Value: Original tetromino type
- Tetromino currently in motion
    - Type (enum)
    - Orientation (enum: North, East, South, West)
    - X position
    - Y position
- Next N tetrominos (should be able to configure N)
- Current Level
- Frames since last move 

## Enums
- Tetrominos
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
    - Next tetrominos top-right
    - Score and level on bottom-right
- Game over screen
