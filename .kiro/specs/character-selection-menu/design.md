# Design Document

## Overview

The character selection feature allows players to choose their preferred character sprite from a visual grid menu. The feature integrates with the existing Player Profile menu system and persists the player's choice using the PlayerConfig system. The design follows the established UI patterns in the codebase, using wooden plank backgrounds, keyboard navigation, and localization support.

## Architecture

The feature consists of three main components:

1. **CharacterSelectionDialog**: A new UI dialog class that displays the character grid and handles user interaction
2. **PlayerConfig Extension**: Extends the existing PlayerConfig class to store and retrieve character selection
3. **Player Class Modification**: Updates the Player class to load character sprites dynamically based on saved configuration

The architecture follows the existing pattern used by LanguageDialog and FontSelectionDialog, ensuring consistency with the codebase.

## Keyboard Controls

The Character Selection Menu uses the following keyboard controls:

- **Arrow Keys (UP/DOWN/LEFT/RIGHT)**: Navigate between character options in the 2x2 grid
- **ENTER**: Confirm the currently selected character and close the dialog
- **ESC**: Cancel selection and close the dialog without saving

This follows the same pattern used by other dialogs in the game (LanguageDialog, FontSelectionDialog).

## Components and Interfaces

### CharacterSelectionDialog

A new UI dialog class responsible for displaying and managing character selection.

**Key Responsibilities:**
- Render a 2x2 grid of character preview cells with wooden plank background
- Handle keyboard navigation (arrow keys for selection, ENTER to confirm, ESC to cancel)
- Display character sprites at 48x48 pixels within 64x64 pixel cells
- Integrate with LocalizationManager for multilingual support
- Notify PlayerConfig when a character is confirmed with ENTER key

**Public Interface:**
```java
public class CharacterSelectionDialog implements LanguageChangeListener {
    public CharacterSelectionDialog()
    public void open()
    public void close()
    public boolean isOpen()
    public void update()
    public void render(SpriteBatch batch, ShapeRenderer shapeRenderer, float camX, float camY)
    public void dispose()
    public void onLanguageChanged(String newLanguage)
}
```

**Character Data Structure:**
```java
private static class CharacterOption {
    String displayName;
    String spriteFilename;
    Texture previewTexture;
    TextureRegion idleFrame;
}
```

### PlayerConfig Extension

Extends the existing PlayerConfig class to include character selection persistence.

**New Methods:**
```java
public String getSelectedCharacter()
public void saveSelectedCharacter(String characterFilename)
```

**JSON Structure Addition:**
```json
{
  "lastServer": "...",
  "language": "...",
  "fontName": "...",
  "selectedCharacter": "boy_navy_start.png",
  "compassTarget": {...}
}
```

### Player Class Modification

Updates the Player class to load character sprites dynamically.

**Modified Method:**
```java
private void loadAnimations() {
    // Load character sprite from PlayerConfig
    PlayerConfig config = PlayerConfig.load();
    String characterSprite = config.getSelectedCharacter();
    
    if (characterSprite == null || characterSprite.isEmpty()) {
        characterSprite = "boy_navy_start.png"; // Default
    }
    
    spriteSheet = new Texture("sprites/player/" + characterSprite);
    // ... rest of animation loading
}
```

### PlayerProfileMenu Extension

Adds "Choose Character" menu option to the existing PlayerProfileMenu.

**Changes:**
- Add "Choose Character" option at index 1 (after "Player Name")
- Handle selection to open CharacterSelectionDialog
- Update menu option count and rendering

## Data Models

### Character Options

The system supports four character sprites:

| Grid Position | Sprite Filename | Display Name |
|--------------|-----------------|--------------|
| Top-Left (0,0) | girl_red_start.png | Girl (Red) |
| Top-Right (1,0) | girl_navy_start.png | Girl (Navy) |
| Bottom-Left (0,1) | boy_red_start.png | Boy (Red) |
| Bottom-Right (1,1) | boy_navy_start.png | Boy (Navy) |

### Grid Navigation

Grid cells are indexed as (column, row):
- Navigation wraps around edges
- UP from row 0 wraps to row 1
- DOWN from row 1 wraps to row 0
- LEFT from column 0 wraps to column 1
- RIGHT from column 1 wraps to column 0

### Sprite Frame Extraction

Character preview frames are extracted from sprite sheets:
- Each sprite sheet contains multiple rows of 64×64 pixel frames
- Idle frame position: (0, 2048) in image editor coordinates (measured from top-left corner)
- For LibGDX TextureRegion extraction, Y-coordinate must be converted from top-left to bottom-left origin
- Conversion formula: `libgdxY = spriteSheetHeight - 2048 - 64`
  - Where 2048 is the Y position from top
  - And 64 is the frame height
- Frame size: 64×64 pixels extracted from sprite sheet
- Display size: 48×48 pixels (scaled down to fit in cell with padding)

**Important**: The sprite sheets for the character selection use a different layout than the existing player sprites. The idle frame for preview is specifically located at image coordinates (0, 2048) from the top-left.


## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system—essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: Grid navigation wrapping

*For any* grid position (column, row) and any navigation direction (UP, DOWN, LEFT, RIGHT), navigating in that direction should move to the correct adjacent cell with wrapping at boundaries. Specifically:
- UP from row 0 wraps to row 1
- DOWN from row 1 wraps to row 0  
- LEFT from column 0 wraps to column 1
- RIGHT from column 1 wraps to column 0

**Validates: Requirements 3.2, 3.3, 3.4, 3.5, 3.6**

### Property 2: Character selection persistence round-trip

*For any* valid character sprite filename, if a user selects that character and confirms the selection, then loading the PlayerConfig should return the same character sprite filename.

**Validates: Requirements 4.1, 5.1, 5.2**

### Property 3: Player loads configured character

*For any* valid character sprite filename stored in PlayerConfig, when the Player class is instantiated, it should load the sprite sheet corresponding to that filename.

**Validates: Requirements 4.3, 4.4, 5.3**

### Property 4: Dialog state transitions

*For any* dialog state (open or closed), performing the appropriate action should transition to the expected state:
- Opening the dialog when closed should result in isOpen() returning true
- Pressing ESC when open should result in isOpen() returning false
- Pressing ENTER to confirm selection when open should result in isOpen() returning false

**Validates: Requirements 1.2, 1.5, 4.2**

### Property 5: Localization usage

*For any* supported language, when that language is active, all text displayed in the Character Selection Menu (title, instructions, character names) should be retrieved from LocalizationManager and match the expected translations for that language.

**Validates: Requirements 1.4, 6.1, 6.2, 6.3, 6.5**

### Property 6: Texture frame extraction

*For any* character sprite sheet, the idle frame extracted for preview should have texture coordinates of (0, 2048) and dimensions of 64×64 pixels.

**Validates: Requirements 2.4**

## Error Handling

### Invalid Character Selection

If the PlayerConfig contains an invalid or non-existent character sprite filename:
- The system logs a warning message
- The system falls back to the default character "boy_navy_start.png"
- The game continues without interruption

### Corrupted Configuration File

If the PlayerConfig JSON file is corrupted or unreadable:
- The system logs an error message
- The system uses default values for all configuration including character selection
- The system attempts to save a new valid configuration file

### Missing Sprite Files

If a character sprite file referenced in the configuration does not exist:
- The system logs an error message
- The system falls back to the default character "boy_navy_start.png"
- The system updates the configuration to use the default character

### Localization Failures

If a translation key is missing from a language file:
- The system logs a warning message
- The system falls back to the English translation
- The menu remains functional with partial translations

## Testing Strategy

### Unit Testing

Unit tests will verify:
- Grid navigation logic correctly calculates next position for all directions
- Character option data structure contains correct sprite filenames
- Menu option array includes "Choose Character" at the correct index
- Default character is "boy_navy_start.png" when config is empty
- Translation keys exist in all language files

### Property-Based Testing

The implementation will use **JUnit with QuickTheories** for property-based testing in Java. Each property-based test will run a minimum of 100 iterations.

Property-based tests will verify:

1. **Grid Navigation Property** (Property 1)
   - Generate random starting positions (column ∈ {0,1}, row ∈ {0,1})
   - Generate random navigation directions (UP, DOWN, LEFT, RIGHT)
   - Verify navigation produces correct wrapped position
   - Tag: `**Feature: character-selection-menu, Property 1: Grid navigation wrapping**`

2. **Persistence Round-Trip Property** (Property 2)
   - Generate random valid character filenames from the set of 4 options
   - Save character selection to config
   - Load config and verify same character is returned
   - Tag: `**Feature: character-selection-menu, Property 2: Character selection persistence round-trip**`

3. **Player Configuration Loading Property** (Property 3)
   - Generate random valid character filenames
   - Set character in PlayerConfig
   - Instantiate Player class
   - Verify Player loads the correct sprite sheet
   - Tag: `**Feature: character-selection-menu, Property 3: Player loads configured character**`

4. **Dialog State Transition Property** (Property 4)
   - Generate random sequences of dialog actions (open, close, confirm, cancel)
   - Verify dialog state matches expected state after each action
   - Tag: `**Feature: character-selection-menu, Property 4: Dialog state transitions**`

5. **Localization Property** (Property 5)
   - Generate random language selections from supported languages
   - Set language in LocalizationManager
   - Verify all dialog text uses LocalizationManager.getText()
   - Verify text changes when language changes
   - Tag: `**Feature: character-selection-menu, Property 5: Localization usage**`

6. **Texture Extraction Property** (Property 6)
   - For each character sprite sheet
   - Extract idle frame texture region
   - Verify coordinates are (0, 2048) and size is 64×64
   - Tag: `**Feature: character-selection-menu, Property 6: Texture frame extraction**`

### Integration Testing

Integration tests will verify:
- CharacterSelectionDialog integrates correctly with PlayerProfileMenu
- Character selection persists across game restarts
- Selected character appears correctly in gameplay
- Language changes update dialog text in real-time
- Dialog follows the same visual style as other game dialogs

### Edge Cases

Edge case tests will verify:
- Empty or missing PlayerConfig defaults to "boy_navy_start.png"
- Corrupted PlayerConfig defaults to "boy_navy_start.png" and logs error
- Invalid character filename in config defaults to "boy_navy_start.png"
- Missing translation keys fall back to English
- Dialog handles rapid keyboard input without state corruption
