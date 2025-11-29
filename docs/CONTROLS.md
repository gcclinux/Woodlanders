# 🎮 Game Controls

## Movement
- **Arrow Keys** - Move character (Up/Down/Left/Right)
- Character automatically animates based on movement direction

## Actions
- **Spacebar** - Context-sensitive action key:
  - **When no item selected**: Attack nearby trees
  - **When item selected**: Plant item at target location
- **Automatic Pickup** - Items are automatically collected when walking near them (within 32 pixels)

## Targeting System (When Item Selected)
When you select a placeable item (e.g., bamboo sapling with key '3'), a white targeting indicator appears:
- **A** - Move target left
- **W** - Move target up
- **D** - Move target right
- **S** - Move target down
- **Spacebar** - Plant item at current target location
- **ESC** - Cancel targeting
- **Press item key again** - Deselect item and hide targeting indicator

The targeting system stays active as long as an item is selected, allowing you to plant multiple items quickly without reactivating targeting.

## Inventory
- **"i" Keys** - "i" to activate inventory slots (toggle selection)
  -  **Arrow Keys** - Move between items (Up/Down/Left/Right)
  - **Spacebar** - Plant item or consume food
- Selected items show a yellow highlight box

## Interface
- **Escape** - Open/close game menu

## Menu Navigation
- **Arrow Keys** or **Up/Down** - Navigate menu options
- **Enter** - Select menu option
- **Escape** - Close menu or cancel dialog
- **Backspace** - Delete character in text input
- **X** - Delete selected save world file (in load menu)
