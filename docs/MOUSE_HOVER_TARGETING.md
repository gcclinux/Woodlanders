# Mouse Hover Targeting Feature

## Overview
Enhanced the targeting system so the white/red indicator dot follows the mouse cursor in real-time when a plantable item is selected, providing immediate visual feedback before clicking to plant.

## Previous Behavior
- Target indicator only updated when mouse button was pressed
- Had to click to see where the target would be placed
- No preview of target position while hovering

## New Behavior
- Target indicator follows mouse cursor continuously (on hover)
- Real-time validation feedback (white = valid, red = invalid)
- See exactly where you'll plant before clicking
- Smooth, responsive targeting experience

## Changes Made

### File: `src/main/java/wagemaker/uk/player/Player.java`

#### Modified Method: `handleTargetingInput()`

**Before:**
```java
// Handle mouse movement for targeting
if (Gdx.input.isTouched()) {
    int mouseX = Gdx.input.getX();
    int mouseY = Gdx.input.getY();
    targetingSystem.setTargetFromMouse(mouseX, mouseY);
}
```

**After:**
```java
// Handle mouse movement for targeting (updates on hover, not just when clicking)
// This allows the target indicator to follow the mouse cursor in real-time
int mouseX = Gdx.input.getX();
int mouseY = Gdx.input.getY();
targetingSystem.setTargetFromMouse(mouseX, mouseY);
```

## Technical Details

### Update Frequency
- Target position updates every frame (60 FPS)
- Mouse coordinates are read continuously
- Validation runs on every position update

### Performance
- Minimal performance impact
- Validation is lightweight (map lookups and biome checks)
- No additional rendering overhead

### Coordinate Conversion
- Screen coordinates → World coordinates via camera.unproject()
- Automatic tile snapping (64x64 grid)
- Range enforcement (if configured)

## User Experience Improvements

### Before:
1. Select plantable item
2. Move mouse to desired location
3. Click to see if position is valid
4. If invalid (red), move and try again
5. Repeat until valid position found

### After:
1. Select plantable item
2. Move mouse around - indicator follows in real-time
3. See validation feedback immediately (white/red)
4. Find valid position visually
5. Click once to plant

## Interaction Flow

1. **Select Plantable Item** (Bamboo/Tree/Banana/Apple Sapling)
   - Targeting system activates automatically
   - Target indicator appears at player position

2. **Move Mouse**
   - Indicator follows mouse cursor smoothly
   - Snaps to 64x64 tile grid
   - Updates validation every frame

3. **Visual Feedback**
   - **White dot**: Valid planting position
     - Correct biome (sand for bamboo, grass for trees)
     - Tile not occupied
     - Item available in inventory
   - **Red dot**: Invalid planting position
     - Wrong biome
     - Tile occupied by another plant/tree
     - Too close to existing tree

4. **Plant Item**
   - **Left Click**: Plant at current target position
   - **P Key**: Plant at current target position
   - **Spacebar**: Plant at current target position

5. **Cancel Targeting**
   - **ESC**: Exit targeting mode
   - **Deselect Item**: Targeting deactivates automatically

## Keyboard Controls (Still Available)

- **A/W/D/S**: Move target cursor by one tile
- **P or Spacebar**: Plant at target
- **ESC**: Cancel targeting

Both mouse and keyboard controls work simultaneously - you can use keyboard to move the cursor, then mouse to fine-tune, or vice versa.

## Benefits

✅ **Immediate Feedback**: See validation before clicking
✅ **Faster Planting**: No trial-and-error clicking
✅ **Better UX**: Intuitive point-and-click interface
✅ **Visual Clarity**: Always know where you'll plant
✅ **Reduced Errors**: Avoid invalid placement attempts

## Testing Recommendations

1. Select bamboo sapling and move mouse over sand/grass - verify indicator changes color
2. Select tree sapling and move mouse over grass/sand - verify indicator changes color
3. Plant a sapling, then hover over it - verify indicator turns red
4. Move mouse quickly - verify indicator follows smoothly without lag
5. Test with keyboard (A/W/D/S) and mouse together - verify both work
6. Test at screen edges - verify coordinate conversion works correctly
