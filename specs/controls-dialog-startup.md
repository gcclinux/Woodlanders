# Controls Dialog on Startup Feature

## Overview
Automatically display the controls dialog when the game starts, allowing players to familiarize themselves with the game controls before playing.

## User Request
Display the controls menu automatically when the game starts (like a splash screen). Players can read the controls and press ESC to close the dialog and begin playing.

## Implementation Summary

### Changes Made

#### 1. GameMenu.java
- Added `hasShownControlsOnStartup` flag to track if controls have been shown in current session
- Added `shouldShowControlsOnStartup` flag to mark that controls should be displayed
- Added `startupFrameCounter` to count frames before showing the dialog
- Created `showControlsOnStartup()` method to mark controls for display
- Created `checkStartupControlsDisplay()` method to handle delayed display logic
- Modified `update()` method to check for startup controls display

#### 2. MyGdxGame.java
- Added call to `gameMenu.showControlsOnStartup()` in the `create()` method after player position is loaded

### Key Features
- Controls dialog appears automatically every time the game launches
- Dialog appears after a 3-frame delay to ensure proper world loading
- Players can press ESC to close the dialog and start playing
- Existing ESC key functionality remains unchanged (open/close menu during gameplay)

## Problem Encountered

### Initial Issue
When the controls dialog was shown immediately on startup, the background world appeared different. After closing the dialog, the world would "change" to show different trees and terrain.

### Root Cause Analysis

**Initialization Sequence:**
1. Game starts with camera at position (0, 0)
2. `gameMenu.loadPlayerPosition()` loads saved player position (e.g., 500, 300)
3. Controls dialog shows immediately (before any rendering)
4. First render frame generates world around camera at (0, 0)
5. When dialog closes, `player.update()` runs and camera moves to saved position (500, 300)
6. World regenerates for new camera view, showing different trees/terrain

**Why This Happened:**
- World generation in this game is **procedural and view-based**
- Trees and objects are generated dynamically based on camera position during rendering
- The `generateTreeAt()` method is called in the render loop for tiles in the camera view
- When camera position changes, different world coordinates are rendered, generating different objects

### The Solution

Implemented a **delayed display mechanism** with a 3-frame wait:

```java
private boolean shouldShowControlsOnStartup = false;
private int startupFrameCounter = 0;

private void checkStartupControlsDisplay() {
    if (shouldShowControlsOnStartup) {
        startupFrameCounter++;
        // Wait 3 frames before showing controls to allow world to load
        if (startupFrameCounter >= 3) {
            controlsDialog.show();
            hasShownControlsOnStartup = true;
            shouldShowControlsOnStartup = false;
        }
    }
}
```

**Why 3 Frames:**
1. **Frame 1**: Camera initializes, player position loads
2. **Frame 2**: Player update runs, camera moves to loaded position
3. **Frame 3**: World generates at correct camera position
4. **Frame 4+**: Controls dialog shows with stable world rendering

This delay is imperceptible to players (< 50ms at 60 FPS) but ensures:
- Camera is positioned at the correct loaded player position
- World has generated around the correct location
- Rendering is stable before the dialog appears
- No visual "jump" or world change when closing the dialog

## Technical Details

### World Generation System
The game uses procedural generation with these characteristics:
- **View-based**: Only generates objects visible in camera view
- **Deterministic**: Uses world seed for consistent generation
- **Dynamic**: Generates on-the-fly during rendering loop
- **Grid-aligned**: Objects spawn on 64px grid coordinates

### Camera and Player Relationship
- Camera follows player position
- Player position can be loaded from save file
- Camera update happens during `player.update()` in render loop
- Initial camera position (0, 0) differs from loaded player position

### Dialog System Integration
- Controls dialog is part of the GameMenu system
- Handled in the dialog priority chain (checked before main menu)
- Uses existing ESC key handling from ControlsDialog class
- No interference with other menu/dialog functionality

## Testing Recommendations

1. **First Launch**: Start game with no save file - controls should appear at spawn (0, 0)
2. **Subsequent Launches**: Start game with existing save - controls should appear at saved position
3. **World Consistency**: Verify world doesn't change when closing controls dialog
4. **ESC Functionality**: Confirm ESC closes controls and normal menu still works
5. **Multiplayer**: Test that controls appear correctly in both singleplayer and multiplayer modes

## Future Enhancements (Optional)

- Add a "Don't show this again" checkbox for experienced players
- Save preference to player config file
- Add animation/fade-in effect for smoother appearance
- Display game tips or hints alongside controls
- Localization support (already implemented via LocalizationManager)

## Files Modified

- `src/main/java/wagemaker/uk/ui/GameMenu.java`
- `src/main/java/wagemaker/uk/gdx/MyGdxGame.java`

## Related Systems

- ControlsDialog: Displays the actual controls information
- LocalizationManager: Provides translated control text
- GameMenu: Manages all menu and dialog states
- Player: Handles position loading and camera following
