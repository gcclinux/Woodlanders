# Design Document

## Overview

This design document outlines the implementation approach for enhancing the Woodlanders game main menu. The enhancement includes adding a title to the menu, introducing a new "Story Mode" placeholder entry, adding white borders around the menu background, and reducing the menu width by 25%. The design maintains compatibility with the existing localization system and menu navigation framework.

## Architecture

The main menu enhancement will be implemented within the existing `GameMenu` class (`src/main/java/wagemaker/uk/ui/GameMenu.java`). The changes are primarily cosmetic and structural, requiring modifications to:

1. **Menu Dimensions**: Update the `MENU_WIDTH` constant to reduce width by 25%
2. **Menu Items Arrays**: Add "Story Mode" entry to both singleplayer and multiplayer menu arrays
3. **Rendering Logic**: Add title rendering and border rendering to the `render()` method
4. **Localization Files**: Add translations for "Main Menu" title and "Story Mode" entry
5. **Menu Action Handler**: Add no-op handler for Story Mode selection

The design follows the existing patterns in the codebase:
- Uses LibGDX rendering primitives (SpriteBatch, ShapeRenderer)
- Integrates with LocalizationManager for multi-language support
- Maintains the existing menu navigation system
- Preserves the wooden plank aesthetic

## Components and Interfaces

### Modified Components

#### GameMenu Class
**Location**: `src/main/java/wagemaker/uk/ui/GameMenu.java`

**Modified Constants**:
```java
private static final float MENU_WIDTH = 300; // Reduced from 400 (25% reduction)
private static final float MENU_HEIGHT = 380; // Increased to accommodate additional menu item
private static final float BORDER_INSET = 5; // New constant for border positioning
```

**Modified Methods**:
- `updateMenuItems()`: Add "Story Mode" entry to menu arrays at correct index
- `render()`: Add title and border rendering
- `executeMenuItem()`: Add Story Mode handler (no-op) and update index handling
- `createWoodenPlank()`: Update to use new width and add white borders
- `handleMultiplayerMenuSelection()`: No changes needed (separate menu)
- `handlePlayerProfileMenuSelection()`: No changes needed (separate menu)

**Menu Index Management**:

Current menu structure (before changes):
```
Index 0: Player Profile
Index 1: Save World
Index 2: Load World
Index 3: Item World (Free World)
Index 4: Multiplayer (or Disconnect in multiplayer mode)
Index 5: Exit
```

New menu structure (after changes):
```
Index 0: Player Profile
Index 1: Save World
Index 2: Load World
Index 3: Item World (Free World)
Index 4: Story Mode (NEW)
Index 5: Multiplayer (or Disconnect in multiplayer mode) - SHIFTED DOWN
Index 6: Exit - SHIFTED DOWN
```

**Critical**: The `executeMenuItem()` method must be updated to handle the new index positions. All index-based logic after index 3 must be incremented by 1.

**New Helper Methods**:
- `renderMenuTitle()`: Renders the localized "Main Menu" title
- `renderMenuBorders()`: Renders white borders around the menu

### Localization Files
**Location**: `assets/localization/*.json`

**New Keys**:
```json
{
  "menu": {
    "title": "Main Menu",
    "story_mode": "Story Mode"
  }
}
```

Languages to update: English (en.json), Polish (pl.json), Portuguese (pt.json), Dutch (nl.json), German (de.json)

## Data Models

No new data models are required. The enhancement uses existing data structures:

- `String[] singleplayerMenuItems`: Extended to include Story Mode
- `String[] multiplayerMenuItems`: Extended to include Story Mode
- `LocalizationManager`: Used for retrieving localized text
- `BitmapFont playerNameFont`: Used for rendering title text

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system-essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: Title Rendering Consistency
*For any* menu state where the main menu is open, the title "Main Menu" should be rendered at the top of the menu background with proper centering
**Validates: Requirements 1.1, 1.3, 1.4**

### Property 2: Title Localization
*For any* supported language, when the language is changed, the menu title should display the correct translation from the localization files
**Validates: Requirements 1.2, 5.2**

### Property 3: Story Mode Entry Presence
*For any* menu rendering, the menu items array should contain the "Story Mode" entry positioned between "Item World" and "Multiplayer"
**Validates: Requirements 2.1, 2.5**

### Property 4: Story Mode Localization
*For any* supported language, the "Story Mode" menu entry should display the correct translation from the localization files
**Validates: Requirements 2.4, 5.3**

### Property 5: Story Mode No-Op Behavior
*For any* game state, when "Story Mode" is selected, no state changes should occur (menu should remain open or close without triggering actions)
**Validates: Requirements 2.3**

### Property 6: Border Rendering
*For any* menu rendering, white borders should be drawn at exactly 5 pixels inset from the menu background edges with white color (1.0, 1.0, 1.0, 1.0)
**Validates: Requirements 3.1, 3.2, 3.3**

### Property 7: Border Dimension Adaptation
*For any* menu background dimensions, the border dimensions should match the background dimensions minus the inset value
**Validates: Requirements 3.4, 4.4**

### Property 8: Menu Width Reduction
*For any* menu rendering, the menu width should be exactly 300 pixels (75% of the original 400 pixels)
**Validates: Requirements 4.1**

### Property 9: Menu Item Positioning
*For any* menu item, the item should be positioned within the bounds of the reduced menu width
**Validates: Requirements 4.2**

### Property 10: Translation Completeness
*For any* supported language file (en, pl, pt, nl, de), the file should contain both "menu.title" and "menu.story_mode" keys
**Validates: Requirements 5.1, 5.5**

### Property 11: Dynamic Language Update
*For any* language change event, all menu text (title and menu entries) should update to reflect the new language
**Validates: Requirements 5.4**

## Error Handling

The enhancement follows existing error handling patterns in the GameMenu class:

1. **Missing Localization Keys**: If localization keys are missing, the LocalizationManager will return the key itself as a fallback
2. **Rendering Errors**: LibGDX handles rendering errors internally; no additional error handling needed
3. **Story Mode Selection**: No-op behavior means no errors can occur from selection

## Testing Strategy

### Unit Testing

Unit tests will verify:
- Menu width constant is correctly set to 300
- Menu items arrays include "Story Mode" entry
- Story Mode is positioned at the correct index
- Localization keys exist in all language files
- Border inset constant is set to 5 pixels

### Property-Based Testing

Property-based tests will use **JUnit with QuickCheck** (or similar Java PBT library) to verify:

1. **Title Centering Property**: Generate random menu widths and verify title is always centered
2. **Localization Completeness Property**: Generate random language selections and verify all required keys exist
3. **Border Positioning Property**: Generate random menu dimensions and verify borders are always 5px inset
4. **Menu Item Bounds Property**: Generate random menu items and verify they fit within menu width

Each property-based test should run a minimum of 100 iterations to ensure thorough coverage.

### Integration Testing

Integration tests will verify:
- Menu renders correctly with all visual elements (title, borders, items)
- Language switching updates all menu text
- Story Mode selection doesn't break menu functionality
- Menu navigation works with the additional menu item

### Manual Testing Checklist

- [ ] Verify title appears at top of menu
- [ ] Verify white borders are visible and properly positioned
- [ ] Verify menu width is visibly narrower
- [ ] Verify Story Mode appears in correct position
- [ ] Test all supported languages display correctly
- [ ] Verify Story Mode selection does nothing (no crash, no action)
- [ ] Verify menu navigation includes Story Mode
- [ ] Test in both singleplayer and multiplayer modes

## Implementation Notes

### Rendering Order

The rendering order in the `render()` method should be:
1. Menu background (wooden plank texture)
2. White borders (using ShapeRenderer)
3. Menu title (using BitmapFont)
4. Menu items (existing rendering logic)

### Menu Height Adjustment

The menu height should be increased from 340 to 380 pixels to accommodate the additional "Story Mode" entry while maintaining proper spacing between items (30 pixels per item).

### Menu Index Update Strategy

**CRITICAL**: When adding "Story Mode" at index 4, all subsequent menu handling logic must be updated:

1. **In `executeMenuItem()` method**: Update the index checks for menu items that come after "Item World"
   - Old: Index 4 was Multiplayer/Disconnect
   - New: Index 4 is Story Mode, Index 5 is Multiplayer/Disconnect
   - Old: Index 5 was Exit
   - New: Index 6 is Exit

2. **Menu array construction in `updateMenuItems()`**:
   ```java
   singleplayerMenuItems = new String[] {
       loc.getText("menu.player_profile"),      // Index 0
       loc.getText("menu.save_world"),          // Index 1
       loc.getText("menu.load_world"),          // Index 2
       loc.getText("menu.free_world"),          // Index 3
       loc.getText("menu.story_mode"),          // Index 4 - NEW
       loc.getText("menu.multiplayer"),         // Index 5 - SHIFTED
       loc.getText("menu.exit")                 // Index 6 - SHIFTED
   };
   
   multiplayerMenuItems = new String[] {
       loc.getText("menu.player_profile"),      // Index 0
       loc.getText("menu.save_world"),          // Index 1
       loc.getText("menu.load_world"),          // Index 2
       loc.getText("menu.free_world"),          // Index 3
       loc.getText("menu.story_mode"),          // Index 4 - NEW
       loc.getText("menu.disconnect"),          // Index 5 - SHIFTED
       loc.getText("menu.exit")                 // Index 6 - SHIFTED
   };
   ```

3. **Testing**: Verify that selecting each menu item triggers the correct action after the index shift

### Story Mode Future Implementation

The Story Mode entry is currently a placeholder. When implementing actual story mode functionality in the future:
1. Update the `executeMenuItem()` method to call a story mode initialization method
2. Create a new `StoryModeManager` class to handle story progression
3. Add story mode state to save/load system
4. Update requirements and design documents for story mode features

### Localization Translations

Suggested translations for "Story Mode":
- English: "Story Mode"
- Polish: "Tryb Fabularny"
- Portuguese: "Modo História"
- Dutch: "Verhaalmodus"
- German: "Story-Modus"

Suggested translations for "Main Menu":
- English: "Main Menu"
- Polish: "Menu Główne"
- Portuguese: "Menu Principal"
- Dutch: "Hoofdmenu"
- German: "Hauptmenü"

## Visual Design Specifications

### Title Specifications
- Font: Use existing `playerNameFont` (FontManager-managed font)
- Color: White (Color.WHITE)
- Position: Centered horizontally, 30 pixels from top of menu
- Size: Default font scale (1.5f as set in GameMenu constructor)

### Border Specifications
- Color: White (1.0f, 1.0f, 1.0f, 1.0f)
- Line Width: 2 pixels (ShapeRenderer default)
- Inset: 5 pixels from menu background edge
- Style: Rectangular outline

### Menu Layout
```
┌─────────────────────┐
│    Main Menu        │  ← Title (30px from top)
│                     │
│  Player Profile     │  ← Menu items start
│─────────────────────│
│    Save World       │
│    Load World       │
│─────────────────────│
│    Item World       │
│    Story Mode       │  ← New entry
│─────────────────────│
│    Multiplayer      │
│                     │
│      Exit           │
└─────────────────────┘
```

## Dependencies

- LibGDX graphics libraries (SpriteBatch, ShapeRenderer, BitmapFont)
- LocalizationManager (existing)
- FontManager (existing)
- GameMenu class (existing)

No new external dependencies are required.
