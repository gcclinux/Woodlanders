# ⚙️ Configuration

## Configuration Directory Locations

Player data and world saves are stored in OS-specific directories:

- **Windows**: `%APPDATA%/Woodlanders/`
- **macOS**: `~/Library/Application Support/Woodlanders/`
- **Linux**: `~/.config/woodlanders/`

## Resource Respawn Configuration

Resource respawn behavior is configured with hardcoded values in the `RespawnConfig.java` class:

- **Default respawn time**: 15 minutes (900,000 ms)
- **Visual indicator threshold**: 1 minute (60,000 ms) before respawn
- **Visual indicators**: Enabled by default
- **Weather System**: Rain events occur randomly every 2-8 minutes, lasting 120 seconds with water puddles
- **Flying Birds**: Bird formations spawn at random intervals (30-90 seconds) and fly across the screen in V-shape patterns
- **Plant Growth**: Baby bamboo grows into harvestable bamboo trees after 120 seconds when planted on sand tiles

To customize respawn durations, modify the constants in `src/main/java/wagemaker/uk/respawn/RespawnConfig.java` and recompile the game.
