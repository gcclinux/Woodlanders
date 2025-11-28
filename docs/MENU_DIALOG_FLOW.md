# Menu and Dialog Flow Documentation

This document maps out all menu and dialog interactions in the game, including how they open, close, and navigate between each other.

## Menu Hierarchy

```
Main Menu (GameMenu)
├── Player Profile Menu (PlayerProfileMenu)
│   ├── Player Name Dialog
│   ├── Player Controls Dialog (ControlsDialog)
│   ├── Player Location Dialog (PlayerLocationDialog)
│   ├── Choose Character Dialog (CharacterSelectionDialog)
│   ├── Font Menu Dialog (FontSelectionDialog)
│   └── Language Dialog (LanguageDialog)
├── Save World Dialog (WorldSaveDialog)
├── Load World Dialog (WorldLoadDialog)
├── Item World (FreeWorldManager)
├── Multiplayer Menu (MultiplayerMenu)
│   ├── Host Server Dialog (ServerHostDialog)
│   └── Connect to Server Dialog (ConnectDialog)
└── Exit
```

## Dialog State Management

### Dialog Source Tracking

The system uses a `DialogSource` enum to track where dialogs were opened from:

```java
private enum DialogSource {
    NONE,        // Default state
    STARTUP,     // Opened automatically at game start
    MAIN_MENU,   // Opened from main menu
    PLAYER_PROFILE // Opened from player profile menu
}
```

This ensures dialogs return to the correct location when closed.

### State Flags

**`isOpen` (Main Menu)**
- `true` = Main menu is visible
- `false` = Main menu is hidden

**`dialogJustClosed`**
- `true` = A dialog was closed this frame (prevents ESC from being processed multiple times)
- `false` = Normal state
- Reset to `false` at start of each frame

**`framesSinceDialogClosed`**
- Counts frames since a dialog was closed (0-5)
- Used to delay input processing after closing a dialog
- Prevents the same ESC key press from being detected multiple times
- Reset to 0 after 5 frames

---

## ESC Key Processing Order

The `update()` method processes input in this order:

1. **Reset flags:** `dialogJustClosed = false`, increment `framesSinceDialogClosed`
2. **Error Dialog** → returns if visible
3. **World Save Dialog** → returns if visible
4. **World Load Dialog** → returns if visible
5. **World Manage Dialog** → returns if visible
6. **Connect Dialog** → returns if visible
7. **Server Host Dialog** → returns if visible
8. **Language Dialog** → returns if visible
9. **Font Selection Dialog** → returns if visible
10. **Player Location Dialog** → returns if visible
    - Sets `dialogJustClosed = true` and `framesSinceDialogClosed = 1` if closed
11. **Controls Dialog** → returns if visible
    - Sets `dialogJustClosed = true` and `framesSinceDialogClosed = 1` if closed
12. **Name Dialog** → returns if visible
13. **Player Profile Menu** → returns if open
    - Skips input processing if `dialogJustClosed = true` or `framesSinceDialogClosed > 0`
    - Prevents opening main menu if a selection was just handled
14. **Multiplayer Menu** → returns if open
15. **Main Menu ESC Toggle**
    - Only if `!playerProfileMenu.isOpen() && !multiplayerMenu.isOpen() && !dialogJustClosed && framesSinceDialogClosed == 0`
16. **Main Menu Navigation** (if `isOpen = true`)

---

## Dialog Opening and Closing Flows

### Controls Dialog at Startup

**Opening:**
1. Game calls `gameMenu.showControlsOnStartup()`
2. After 3 frames, `checkStartupControlsDisplay()` sets `controlsDialogSource = DialogSource.STARTUP`
3. Opens controls dialog

**Closing (ESC pressed):**
1. Dialog closes, sets `dialogJustClosed = true` and `framesSinceDialogClosed = 1`
2. `handleControlsDialogResult()` sees source is `STARTUP`
3. Returns to game (no menu opens) ✓

### Controls Dialog from Player Profile

**Opening:**
1. User navigates: Main Menu → Player Profile → Player Controls
2. `openControlsDialogFromProfile()` sets `controlsDialogSource = DialogSource.PLAYER_PROFILE`
3. Opens controls dialog

**Closing (ESC pressed):**
1. Dialog closes, sets `dialogJustClosed = true` and `framesSinceDialogClosed = 1`
2. `handleControlsDialogResult()` sees source is `PLAYER_PROFILE`
3. Opens player profile menu
4. For next 5 frames, player profile menu skips input processing
5. After 5 frames, player profile menu resumes normal operation ✓

### Player Location Dialog

Works identically to Controls Dialog with its own `playerLocationDialogSource` tracking.

---

## All Dialog Return Paths

| Dialog | Opened From | ESC Behavior | Return To |
|--------|-------------|--------------|-----------|
| Controls | Startup | Close | Game (no menu) |
| Controls | Main Menu | Close | Main Menu |
| Controls | Player Profile | Close | Player Profile |
| Player Location | Main Menu | Close | Main Menu |
| Player Location | Player Profile | Close | Player Profile |
| Player Name | Main Menu | Close/Save | Main Menu |
| Player Name | Player Profile | Close/Save | Player Profile |
| Character Selection | Player Profile | Close/Select | Player Profile |
| Font Selection | Player Profile | Close/Select | Player Profile |
| Language | Player Profile | Close/Select | Player Profile |
| World Save | Main Menu | Close/Save | Main Menu |
| World Load | Main Menu | Close/Load | Main Menu |
| Error | Any | Close | Previous screen |
| Connect | Multiplayer Menu | Close/Connect | Multiplayer Menu |
| Server Host | Multiplayer Menu | Close | Multiplayer Menu |

---

## Testing Checklist

- [ ] Open controls at startup, press ESC → Should close, no menu opens
- [ ] Main Menu → Player Profile → Controls, press ESC → Should return to Player Profile
- [ ] Main Menu → Player Profile → Player Location, press ESC → Should return to Player Profile
- [ ] Main Menu → Player Profile → Player Name, press ESC → Should return to Player Profile
- [ ] Main Menu → Player Profile → Character, press ESC → Should return to Player Profile
- [ ] Main Menu → Player Profile → Font, press ESC → Should return to Player Profile
- [ ] Main Menu → Player Profile → Language, press ESC → Should return to Player Profile
- [ ] Main Menu → Player Profile, press ESC → Should return to Main Menu
- [ ] Main Menu, press ESC → Should close Main Menu (return to game)
