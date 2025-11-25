# Requirements Document

## Introduction

This feature extends the existing weather system's water puddle mechanics by adding player interaction. When a player walks over a puddle, they will fall, take damage, and display a fall-and-standup animation sequence before returning to normal movement. This adds environmental hazards and visual feedback to the rain system.

## Glossary

- **Puddle System**: The existing weather system component that creates water puddles after rain accumulation
- **Player**: The controllable character entity in the game
- **Fall Zone**: A 12x12 pixel area from the center of each puddle that triggers the fall mechanic
- **Fall Animation Sequence**: A series of 5 sprite frames showing the player falling and standing up
- **Sprite Sheet**: The texture containing all player animation frames in a 64x64 grid
- **Player Config**: The configuration system that stores the selected character sprite sheet

## Requirements

### Requirement 1

**User Story:** As a player, I want puddles to be interactive hazards, so that the environment feels more dynamic and challenging.

#### Acceptance Criteria

1. WHEN a player's position intersects with a puddle's fall zone THEN the system SHALL trigger the fall mechanic
2. WHEN the fall mechanic triggers THEN the system SHALL apply 10% damage to the player's health
3. WHEN a player falls THEN the system SHALL prevent normal player movement until the standup sequence completes
4. WHEN the standup sequence completes THEN the system SHALL restore normal player movement and sprite rendering
5. WHEN rain stops and puddles disappear THEN the system SHALL clear all puddle collision data

### Requirement 2

**User Story:** As a player, I want to see a fall-and-standup animation when I step in a puddle, so that the interaction feels realistic and provides clear visual feedback.

#### Acceptance Criteria

1. WHEN the fall mechanic triggers THEN the system SHALL display the fall sprite at coordinates (256, 1280) from the player's sprite sheet
2. WHEN 0.8 seconds elapse THEN the system SHALL display standup1 sprite at coordinates (192, 1280)
3. WHEN another 0.8 seconds elapse THEN the system SHALL display standup2 sprite at coordinates (128, 1280)
4. WHEN another 0.8 seconds elapse THEN the system SHALL display standup3 sprite at coordinates (64, 1280)
5. WHEN another 0.8 seconds elapse THEN the system SHALL display standup4 sprite at coordinates (0, 1280)
6. WHEN standup4 completes THEN the system SHALL return to normal directional sprite rendering
7. WHEN rendering fall/standup sprites THEN the system SHALL use the same sprite sheet configured in PlayerConfig

### Requirement 3

**User Story:** As a developer, I want puddles to track their collision zones, so that the fall mechanic can detect player intersection efficiently.

#### Acceptance Criteria

1. WHEN a puddle spawns THEN the system SHALL record its center coordinates
2. WHEN checking for player collision THEN the system SHALL calculate distance from player center to puddle center
3. WHEN the distance is less than or equal to 12 pixels THEN the system SHALL consider the player within the fall zone
4. WHEN puddles evaporate THEN the system SHALL remove their collision data from memory
5. WHEN the puddle system transitions to NONE state THEN the system SHALL clear all puddle collision records

### Requirement 4

**User Story:** As a player, I want to only fall once per puddle encounter, so that I don't get stuck in a fall loop.

#### Acceptance Criteria

1. WHEN a player falls in a puddle THEN the system SHALL mark that puddle as "triggered" for that player
2. WHEN a player is within a triggered puddle's fall zone THEN the system SHALL not trigger the fall mechanic again
3. WHEN a player exits a puddle's fall zone THEN the system SHALL reset the triggered state for that puddle
4. WHEN puddles despawn THEN the system SHALL clear all triggered states
5. WHEN a player is in the fall/standup animation sequence THEN the system SHALL ignore all other puddle collision checks

### Requirement 5

**User Story:** As a developer, I want the fall animation system to integrate with the existing player rendering, so that it works seamlessly with all character sprites.

#### Acceptance Criteria

1. WHEN extracting fall/standup frames THEN the system SHALL use 64x64 pixel regions from the sprite sheet
2. WHEN calculating sprite coordinates THEN the system SHALL use top-left origin (0,0) at the top of the sprite sheet
3. WHEN the player sprite sheet changes via PlayerConfig THEN the system SHALL use the new sprite sheet for fall/standup frames
4. WHEN rendering fall/standup frames THEN the system SHALL maintain the same screen position as normal player rendering
5. WHEN the fall sequence is active THEN the system SHALL not render normal walking or idle animations
