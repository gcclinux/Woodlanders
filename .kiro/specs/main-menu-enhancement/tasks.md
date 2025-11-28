# Implementation Plan

- [x] 1. Update localization files with new menu entries

  - Add "menu.title" and "menu.story_mode" keys to all language files (en, pl, pt, nl, de)
  - Ensure translations are accurate and consistent with existing terminology
  - _Requirements: 1.2, 2.4, 5.1, 5.2, 5.3, 5.5_

- [x] 2. Update GameMenu constants and menu structure

  - [x] 2.1 Reduce MENU_WIDTH constant from 400 to 300 (25% reduction)

    - Update the constant definition
    - _Requirements: 4.1_
  
  - [x] 2.2 Increase MENU_HEIGHT constant from 340 to 380

    - Update to accommodate the additional menu item
    - _Requirements: 2.1_
  
  - [x] 2.3 Add BORDER_INSET constant set to 5

    - Define new constant for border positioning
    - _Requirements: 3.2_
  
  - [x] 2.4 Update menu items arrays in updateMenuItems() method

    - Add "Story Mode" entry at index 4 in both singleplayerMenuItems and multiplayerMenuItems
    - Verify Multiplayer/Disconnect moves to index 5
    - Verify Exit moves to index 6
    - _Requirements: 2.1, 2.5_

- [x] 3. Update menu action handler for new indices

  - [x] 3.1 Modify executeMenuItem() method to handle Story Mode at index 4

    - Add case for Story Mode that does nothing (no-op placeholder)
    - Update index checks for Multiplayer/Disconnect (now index 5)
    - Update index checks for Exit (now index 6)
    - Ensure all menu actions trigger correctly after index shift
    - _Requirements: 2.3, 2.5_

- [x] 4. Implement menu title rendering


  - [x] 4.1 Create renderMenuTitle() helper method

    - Retrieve localized "menu.title" text from LocalizationManager
    - Calculate centered X position using GlyphLayout
    - Position title 30 pixels from top of menu
    - Render using playerNameFont with white color
    - _Requirements: 1.1, 1.2, 1.3, 1.4, 5.2_
  
  - [x] 4.2 Integrate title rendering into render() method

    - Call renderMenuTitle() after drawing menu background
    - Ensure title renders before menu items
    - _Requirements: 1.1_

- [x] 5. Implement white border rendering

  - [x] 5.1 Create renderMenuBorders() helper method

    - Use ShapeRenderer to draw rectangular border
    - Position border 5 pixels inset from menu edges
    - Use white color (1.0f, 1.0f, 1.0f, 1.0f)
    - Calculate border dimensions based on MENU_WIDTH and MENU_HEIGHT
    - _Requirements: 3.1, 3.2, 3.3, 3.4_
  
  - [x] 5.2 Integrate border rendering into render() method

    - Call renderMenuBorders() after drawing menu background but before title
    - Use ShapeRenderer.ShapeType.Line for border outline
    - _Requirements: 3.1_

- [x] 6. Update createWoodenPlank() method

  - Verify wooden plank texture uses updated MENU_WIDTH constant
  - Ensure texture generation works correctly with new dimensions
  - _Requirements: 4.1, 4.4_

- [x] 7. Checkpoint - Verify menu rendering and functionality

  - Ensure all tests pass, ask the user if questions arise
  - Manually verify menu displays correctly with title and borders
  - Verify all menu items are selectable and trigger correct actions
  - Test in both singleplayer and multiplayer modes
  - Test language switching updates all menu text

- [ ] 8. Write unit tests for menu structure
  - Test MENU_WIDTH is 300
  - Test MENU_HEIGHT is 380
  - Test BORDER_INSET is 5
  - Test menu arrays contain Story Mode at index 4
  - Test menu arrays have correct length (7 items)
  - _Requirements: 2.1, 4.1_

- [ ] 9. Write property-based tests
  - [ ] 9.1 Property test for title centering
    - **Property 1: Title Rendering Consistency**
    - **Validates: Requirements 1.1, 1.3, 1.4**
    - Generate random menu widths and verify title is centered
  
  - [ ] 9.2 Property test for title localization
    - **Property 2: Title Localization**
    - **Validates: Requirements 1.2, 5.2**
    - Test all supported languages display correct title translation
  
  - [ ] 9.3 Property test for Story Mode localization
    - **Property 4: Story Mode Localization**
    - **Validates: Requirements 2.4, 5.3**
    - Test all supported languages display correct Story Mode translation
  
  - [ ] 9.4 Property test for border positioning
    - **Property 6: Border Rendering**
    - **Validates: Requirements 3.1, 3.2, 3.3**
    - Verify borders are exactly 5px inset with white color
  
  - [ ] 9.5 Property test for translation completeness
    - **Property 10: Translation Completeness**
    - **Validates: Requirements 5.1, 5.5**
    - Verify all language files contain required keys

- [ ] 10. Write integration tests
  - Test menu renders with all visual elements
  - Test language switching updates menu text
  - Test Story Mode selection doesn't break functionality
  - Test menu navigation works with additional item
  - _Requirements: 1.1, 1.2, 2.1, 2.2, 2.3, 5.4_
