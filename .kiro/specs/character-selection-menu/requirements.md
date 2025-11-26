# Requirements Document

## Introduction

This document specifies the requirements for a character selection feature that allows players to choose their preferred character sprite from a visual menu. The feature will be accessible through the Player Profile menu and will persist the player's choice across game sessions.

## Glossary

- **Character Selection Menu**: A dialog window displaying available character sprites in a grid layout for player selection
- **Player Profile Menu**: The existing menu accessed from Main Menu → Player Profile
- **Character Sprite**: A visual representation of the player character, stored as a PNG file with animation frames
- **Player Config**: A JSON configuration file that stores player preferences including character selection
- **Sprite Sheet**: A texture file containing multiple animation frames for a character
- **Grid Cell**: A 64x64 pixel square container in the character selection menu that displays a character preview

## Requirements

### Requirement 1

**User Story:** As a player, I want to access a character selection menu from the Player Profile menu, so that I can choose my preferred character appearance.

#### Acceptance Criteria

1. WHEN the Player Profile menu is displayed THEN the system SHALL include a "Choose Character" menu option positioned below "Player Name"
2. WHEN a user selects the "Choose Character" option THEN the system SHALL open the Character Selection Menu centered on the screen
3. WHEN the Character Selection Menu opens THEN the system SHALL display the menu with a wooden plank background style consistent with other game menus
4. WHEN the Character Selection Menu is displayed THEN the system SHALL render the menu title using the current localized text
5. WHEN the user presses ESC while the Character Selection Menu is open THEN the system SHALL close the menu and return to the Player Profile menu

### Requirement 2

**User Story:** As a player, I want to see available character options displayed in a grid layout, so that I can visually compare and select my preferred character.

#### Acceptance Criteria

1. WHEN the Character Selection Menu is displayed THEN the system SHALL render a 2x4 grid of character preview cells (2 columns, 4 rows)
2. WHEN rendering each grid cell THEN the system SHALL draw a 128x128 pixel square with a 2-pixel white border
3. WHEN rendering character previews THEN the system SHALL display each character sprite at 96x96 pixels centered within its 128x128 pixel cell
4. WHEN rendering character previews THEN the system SHALL extract the idle frame from position (0, 1664) of each character sprite sheet
5. WHEN displaying the grid THEN the system SHALL show the following characters in order: girl_red_start.png (row 0, col 0), girl_navy_start.png (row 0, col 1), girl_green_start.png (row 1, col 0), girl_walnut_start.png (row 1, col 1), boy_red_start.png (row 2, col 0), boy_navy_start.png (row 2, col 1), boy_green_start.png (row 3, col 0), boy_walnut_start.png (row 3, col 1)

### Requirement 3

**User Story:** As a player, I want to navigate through character options using keyboard controls, so that I can select my preferred character.

#### Acceptance Criteria

1. WHEN the Character Selection Menu is open THEN the system SHALL highlight the currently selected character cell with a yellow border
2. WHEN the user presses the UP arrow key THEN the system SHALL move the selection to the cell above the current selection
3. WHEN the user presses the DOWN arrow key THEN the system SHALL move the selection to the cell below the current selection
4. WHEN the user presses the LEFT arrow key THEN the system SHALL move the selection to the cell to the left of the current selection
5. WHEN the user presses the RIGHT arrow key THEN the system SHALL move the selection to the cell to the right of the current selection
6. WHEN the user navigates beyond the grid boundaries THEN the system SHALL wrap the selection to the opposite side of the grid (UP from row 0 wraps to row 3, DOWN from row 3 wraps to row 0, LEFT from column 0 wraps to column 1, RIGHT from column 1 wraps to column 0)

### Requirement 4

**User Story:** As a player, I want to confirm my character selection, so that my chosen character is used in the game.

#### Acceptance Criteria

1. WHEN the user presses ENTER while a character is selected THEN the system SHALL save the selected character sprite path to the Player Config
2. WHEN the user confirms a character selection THEN the system SHALL close the Character Selection Menu
3. WHEN the user confirms a character selection THEN the system SHALL update the Player class to load the selected character sprite
4. WHEN the Player class loads animations THEN the system SHALL use the character sprite path stored in Player Config
5. WHEN no character selection has been saved THEN the system SHALL default to "boy_navy_start.png" as the character sprite

### Requirement 5

**User Story:** As a player, I want my character selection to persist across game sessions, so that I don't have to reselect my character every time I play.

#### Acceptance Criteria

1. WHEN a character selection is confirmed THEN the system SHALL write the character sprite filename to the Player Config JSON file
2. WHEN the game starts THEN the system SHALL read the saved character selection from the Player Config
3. WHEN the Player class is instantiated THEN the system SHALL load the character sprite specified in the Player Config
4. WHEN the Player Config does not contain a character selection THEN the system SHALL use "boy_navy_start.png" as the default character
5. WHEN the Player Config file is corrupted or unreadable THEN the system SHALL use "boy_navy_start.png" as the default character and log an error

### Requirement 6

**User Story:** As a multilingual player, I want the character selection menu to display in my chosen language, so that I can understand the interface.

#### Acceptance Criteria

1. WHEN the Character Selection Menu is displayed THEN the system SHALL render all text using the LocalizationManager
2. WHEN the user changes the game language THEN the system SHALL update the Character Selection Menu text to the new language
3. WHEN the Character Selection Menu is opened THEN the system SHALL display the menu title in the current language
4. WHEN localization files are loaded THEN the system SHALL include translations for "Choose Character" and all 8 character names (Girl Red, Girl Navy, Girl Green, Girl Walnut, Boy Red, Boy Navy, Boy Green, Boy Walnut) in all supported languages (English, German, Dutch, Polish, Portuguese)
5. WHEN the Character Selection Menu displays instructions THEN the system SHALL show keyboard navigation hints in the current language
