# Implementation Plan

- [x] 1. Add localization keys for character selection menu
  - Add "character_selection_dialog" section to all language files (en.json, de.json, nl.json, pl.json, pt.json)
  - Include keys for: title, instructions, character names, navigation hints
  - Add "choose_character" key to "player_profile_menu" section in all language files
  - _Requirements: 6.4_

- [x] 2. Extend PlayerConfig to support character selection
- [x] 2.1 Add character selection fields to PlayerConfig
  - Add `selectedCharacter` field to PlayerConfig class
  - Implement `getSelectedCharacter()` method that returns the saved character filename
  - Implement `saveSelectedCharacter(String filename)` method that persists to JSON
  - Add default value "boy_navy_start.png" when no character is saved
  - _Requirements: 4.5, 5.4_

- [x] 2.2 Write property test for character selection persistence
  - **Property 2: Character selection persistence round-trip**
  - **Validates: Requirements 4.1, 5.1, 5.2**

- [x] 2.3 Write unit tests for PlayerConfig character methods
  - Test getSelectedCharacter() returns correct value
  - Test saveSelectedCharacter() writes to JSON correctly
  - Test default value when config is empty
  - Test error handling for corrupted config
  - _Requirements: 4.5, 5.4, 5.5_

- [x] 3. Create CharacterSelectionDialog class
- [x] 3.1 Implement basic dialog structure
  - Create CharacterSelectionDialog class implementing LanguageChangeListener
  - Add wooden plank background texture generation (consistent with other dialogs)
  - Implement open(), close(), isOpen() methods
  - Add dialog font creation using FontManager
  - Implement dispose() method for resource cleanup
  - _Requirements: 1.2, 1.3, 1.5_

- [x] 3.2 Implement character options data structure
  - Create CharacterOption inner class with displayName, spriteFilename, previewTexture, idleFrame fields
  - Initialize array of 4 character options: girl_red_start.png, girl_navy_start.png, boy_red_start.png, boy_navy_start.png
  - Load sprite sheets for each character
  - Extract idle frame from position (0, 2048) using LibGDX coordinate conversion
  - _Requirements: 2.4, 2.5_

- [x] 3.3 Write property test for texture frame extraction
  - **Property 6: Texture frame extraction**
  - **Validates: Requirements 2.4**

- [x] 3.4 Implement grid navigation logic
  - Add selectedRow and selectedColumn fields (0-1 range)
  - Implement navigation methods: navigateUp(), navigateDown(), navigateLeft(), navigateRight()
  - Implement wrapping logic at grid boundaries
  - _Requirements: 3.2, 3.3, 3.4, 3.5, 3.6_

- [x] 3.5 Write property test for grid navigation
  - **Property 1: Grid navigation wrapping**
  - **Validates: Requirements 3.2, 3.3, 3.4, 3.5, 3.6**

- [x] 3.6 Implement keyboard input handling
  - Add update() method to handle keyboard input
  - Handle arrow keys for navigation (UP, DOWN, LEFT, RIGHT)
  - Handle ENTER key to confirm selection and save to PlayerConfig
  - Handle ESC key to cancel and close dialog
  - _Requirements: 3.2, 3.3, 3.4, 3.5, 4.1, 4.2, 1.5_

- [x] 3.7 Write property test for dialog state transitions
  - **Property 4: Dialog state transitions**
  - **Validates: Requirements 1.2, 1.5, 4.2**

- [x] 3.8 Implement dialog rendering
  - Implement render() method with SpriteBatch and ShapeRenderer parameters
  - Draw wooden plank background centered on camera
  - Render dialog title using LocalizationManager
  - Render 2x2 grid of 64x64 pixel cells with 2-pixel white borders
  - Render character preview sprites at 48x48 pixels centered in cells
  - Highlight selected cell with yellow border
  - Render keyboard navigation instructions at bottom
  - _Requirements: 1.3, 1.4, 2.1, 2.2, 2.3, 2.5, 3.1_

- [x] 3.9 Write property test for localization usage
  - **Property 5: Localization usage**
  - **Validates: Requirements 1.4, 6.1, 6.2, 6.3, 6.5**

- [x] 3.10 Implement language change listener
  - Implement onLanguageChanged() method
  - Ensure dialog text updates when language changes
  - Register/unregister with LocalizationManager in constructor/dispose
  - _Requirements: 6.2_

- [x] 4. Integrate CharacterSelectionDialog with PlayerProfileMenu
- [x] 4.1 Add "Choose Character" menu option
  - Update PlayerProfileMenu menuOptions array to include "Choose Character" at index 1
  - Update menu rendering to display new option
  - Adjust menu height if needed to accommodate new option
  - _Requirements: 1.1_

- [x] 4.2 Handle character selection menu opening
  - Add CharacterSelectionDialog instance to PlayerProfileMenu
  - Handle selection of "Choose Character" option to open dialog
  - Update PlayerProfileMenu.update() to call dialog.update() when dialog is open
  - Update PlayerProfileMenu.render() to call dialog.render() when dialog is open
  - _Requirements: 1.2_

- [x] 4.3 Handle dialog lifecycle
  - Ensure dialog is properly disposed when PlayerProfileMenu is disposed
  - Prevent PlayerProfileMenu input handling when dialog is open
  - Return to PlayerProfileMenu when dialog closes
  - _Requirements: 1.5_

- [x] 4.4 Write integration test for menu integration
  - Test "Choose Character" option appears in correct position
  - Test selecting option opens dialog
  - Test dialog closes and returns to menu
  - _Requirements: 1.1, 1.2, 1.5_

- [x] 5. Update Player class to use selected character
- [x] 5.1 Modify Player.loadAnimations() to load from config
  - Load PlayerConfig at start of loadAnimations()
  - Get selected character filename from config
  - Use default "boy_navy_start.png" if config is empty or invalid
  - Load sprite sheet using selected character filename
  - Add error handling for missing sprite files
  - _Requirements: 4.3, 4.4, 4.5, 5.3, 5.4_

- [x] 5.2 Write property test for player configuration loading
  - **Property 3: Player loads configured character**
  - **Validates: Requirements 4.3, 4.4, 5.3**

- [x] 5.3 Write unit tests for Player character loading
  - Test Player loads correct sprite when config has valid character
  - Test Player uses default when config is empty
  - Test Player handles missing sprite files gracefully
  - _Requirements: 4.3, 4.4, 4.5, 5.3, 5.4, 5.5_

- [x] 6. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.
