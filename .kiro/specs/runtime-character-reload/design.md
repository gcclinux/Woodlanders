# Design Document

## Overview

The runtime character reload feature enables players to change their character appearance immediately after selection without restarting the game. This is achieved by adding a public `reloadCharacter()` method to the Player class that disposes the old sprite sheet and loads the new one, recreating all animations. The reload is automatically triggered by GameMenu after saving when a character change is detected.

## Architecture

The feature consists of two main components:

1. **Player.reloadCharacter()**: A public method that handles the sprite reload process
2. **GameMenu.savePlayerPosition()**: Enhanced to detect character changes and trigger reload

The architecture follows a simple call pattern:
```
CharacterSelectionDialog → GameMenu.setPendingCharacterSelection()
                         ↓
User selects "Save Player"
                         ↓
GameMenu.savePlayerPosition() → Saves to disk
                         ↓
Detects character change → Player.reloadCharacter()
                         ↓
Clears pending selection
```

## Components and Interfaces

### Player.reloadCharacter()

A public method that reloads the player's character sprite at runtime.

**Signature:**
```java
public void reloadCharacter()
```

**Responsibilities:**
- Dispose the old sprite sheet texture to free GPU memory
- Call loadAnimations() to load the new character sprite
- Recreate all animation frames and idle frames
- Log the reload process for debugging

**Error Handling:**
- If the new sprite fails to load, loadAnimations() will fall back to the default character
- If the default also fails, throws RuntimeException (same as constructor behavior)

### GameMenu.savePlayerPosition() Enhancement

Enhanced to detect and handle character changes.

**New Logic:**
```java
// Track if character was changed
boolean characterWasChanged = false;
if (pendingCharacterSelection != null) {
    selectedCharacter = pendingCharacterSelection;
    characterWasChanged = true;
}

// After save completes
if (characterWasChanged) {
    player.reloadCharacter();
    clearPendingCharacterSelection();
}
```

**Responsibilities:**
- Detect if a character change is pending
- Save the new character to woodlanders.json
- Trigger player sprite reload if character changed
- Clear the pending selection after reload

## Data Flow

### Character Selection and Reload Flow

1. **Selection Phase**:
   - User opens Character Selection Dialog
   - User navigates and selects a character
   - User presses ENTER
   - CharacterSelectionDialog calls `GameMenu.setPendingCharacterSelection(filename)`
   - Dialog closes

2. **Save Phase**:
   - User selects "Save Player" from menu
   - GameMenu.savePlayerPosition() is called
   - Method checks if `pendingCharacterSelection != null`
   - If yes, sets `characterWasChanged = true`
   - Saves character to woodlanders.json

3. **Reload Phase**:
   - After save completes, checks `characterWasChanged`
   - If true, calls `player.reloadCharacter()`
   - Player disposes old sprite sheet
   - Player loads new sprite sheet from PlayerConfig
   - Player recreates all animations
   - GameMenu clears pending selection

### Memory Management

**Old Sprite Sheet Disposal:**
```java
if (spriteSheet != null) {
    spriteSheet.dispose();  // Frees GPU memory
}
```

**New Sprite Sheet Loading:**
```java
spriteSheet = new Texture("sprites/player/" + characterFilename);
```

**Animation Recreation:**
- All TextureRegion arrays are recreated
- All Animation objects are recreated
- All idle frames are recreated
- Old references are garbage collected

## State Preservation

The reload process preserves all player state:

**Preserved State:**
- Position (x, y coordinates)
- Health (current health value)
- Hunger (current hunger value)
- Direction (current facing direction)
- Inventory (all items)
- Animation time (continues smoothly)

**Not Preserved:**
- Sprite sheet texture (intentionally replaced)
- Animation frames (intentionally recreated)
- Idle frames (intentionally recreated)

## Error Handling

### Sprite Load Failure

If the new character sprite fails to load:
1. Log error message with sprite filename
2. Fall back to default character "boy_navy_start.png"
3. Attempt to load default sprite
4. If default also fails, throw RuntimeException

### Disposal Failure

If sprite disposal fails:
- LibGDX handles disposal errors internally
- Worst case: minor memory leak of one texture
- Next reload will dispose the leaked texture

### Save Failure

If save operation fails:
- Character reload is not triggered
- Pending selection remains in memory
- User can try saving again

## Performance Considerations

### Reload Time

- Texture disposal: ~1ms
- Texture loading: ~10-50ms (depends on file size)
- Animation creation: ~1ms
- Total: ~12-52ms (imperceptible to user)

### Memory Usage

- Old sprite sheet: ~2-4 MB GPU memory
- New sprite sheet: ~2-4 MB GPU memory
- Peak during reload: ~4-8 MB (both in memory briefly)
- After disposal: ~2-4 MB (only new sprite)

### Frame Rate Impact

- Reload happens during save operation
- Save already shows notification overlay
- No visible frame rate drop
- Game continues running normally

## Testing Strategy

### Manual Testing

1. **Basic Reload Test**:
   - Select a character different from current
   - Save player
   - Verify character sprite changes immediately
   - Verify no visual glitches

2. **Multiple Reload Test**:
   - Change character and save 5 times
   - Verify each change applies correctly
   - Monitor memory usage (should stay stable)

3. **State Preservation Test**:
   - Move to a specific position
   - Take damage to reduce health
   - Wait for hunger to increase
   - Change character and save
   - Verify position, health, hunger unchanged

4. **Error Handling Test**:
   - Modify code to use invalid sprite filename
   - Verify fallback to default character
   - Verify game doesn't crash

### Integration Testing

1. **Singleplayer Mode**:
   - Test character reload in singleplayer
   - Verify sprite changes correctly
   - Verify save/load cycle works

2. **Multiplayer Mode**:
   - Test character reload in multiplayer
   - Verify sprite changes for local player
   - Verify other players see the change (after reconnect)

3. **Save/Load Cycle**:
   - Change character and save
   - Exit game
   - Restart game
   - Verify new character loads correctly

## Implementation Notes

### Why Public Method?

The `reloadCharacter()` method is public because:
- Needs to be called from GameMenu (different package)
- May be useful for future features (e.g., character customization)
- Follows principle of exposing useful functionality

### Why Not Automatic?

The reload is not automatic on character selection because:
- User might want to preview before committing
- Aligns with save-based workflow
- Gives user control over when changes apply
- Consistent with other settings (language, font)

### Thread Safety

The reload is thread-safe because:
- Called from main render thread (same as save)
- No concurrent access to sprite sheet
- LibGDX texture operations are not thread-safe, so must be on render thread

## Future Enhancements

### Possible Improvements

1. **Smooth Transition**: Add fade effect during reload
2. **Preview Mode**: Show character preview before committing
3. **Undo Feature**: Allow reverting to previous character
4. **Character Customization**: Extend to support color variations
5. **Multiplayer Sync**: Broadcast character changes to other players immediately

### API Extensions

```java
// Reload with specific character (bypass config)
public void reloadCharacter(String characterFilename)

// Check if reload is needed
public boolean needsCharacterReload()

// Get current character filename
public String getCurrentCharacterFilename()
```
