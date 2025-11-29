# 🎯 Game Overview

A 2D top-down multiplayer adventure game built with libGDX featuring infinite world exploration, animated characters, tree chopping mechanics, and real-time multiplayer gameplay.

## Performance Features

- Chunk-based rendering (only visible areas)
- Optimized collision detection with spatial partitioning
- Efficient network message batching
- Delta-time based animations and physics
- Texture atlas for sprite management
- Memory-efficient world generation

## Network Architecture

- **Protocol**: Custom TCP-based protocol
- **Message Types**: 20+ synchronized message types
- **Synchronization**: Server-authoritative with client prediction
- **Heartbeat**: 5-second keepalive with 15-second timeout
- **Rate Limiting**: Configurable message rate limits per client

## 🎮 Game Features

### World & Environment
- ✨ **Infinite Procedurally Generated World** - Explore endlessly with dynamic terrain generation
- 🏜️ **Multiple Biomes** - Grass and sand biomes with distinct visual styles
- 🌧️ **Dynamic Weather System** - Random rain events that follow the player (120s duration, 2-8 minute intervals)
- 🧭 **Compass Navigation** - Always points toward spawn point for easy navigation
- 💾 **World Save/Load System** - Save and load complete world states with separate singleplayer/multiplayer saves
- 💧 **Water Puddles** - Puddles form during rain and evaporate after rain stops, adding environmental realism
- 🦅 **Flying Birds** - Ambient bird formations fly across the screen in V-shape patterns from random boundaries

### Character & Movement
- 🏃 **Animated Player Character** - Smooth walking animations with directional sprites
- 💚 **Health & Hunger System** - Dual survival mechanics with health damage and hunger accumulation
- 🍎 **Apple Consumption** - Restores 10% health when consumed (press number key to select, space to consume)
- 🍌 **Banana Consumption** - Reduces 5% hunger when consumed (press number key to select, space to consume)
- 📊 **Unified Health Bar** - Visual display showing both health (red) and hunger (blue) status
- 🎯 **Precise Collision Detection** - Optimized hitboxes for all game objects
- 👤 **Character Selection** - Choose from 4 character sprites (2 girls, 2 boys) with red or navy outfits; changes apply immediately after save

### Trees & Resources
- 🌳 **Multiple Tree Types** - Small trees, regular trees, apple trees, banana trees, bamboo trees, and coconut trees
- ⚔️ **Combat System** - Attack and destroy trees with visual health bars
- 🔄 **Health Regeneration** - Damaged trees slowly recover health over time
- 🌵 **Environmental Hazards** - Cacti that damage players on contact
- 🎋 **Bamboo Planting System** - Plant bamboo sapling on sand tiles using the targeting system; grows into harvestable bamboo trees (120s growth time)
- 🎯 **Tile Targeting System** - Visual targeting indicator for precise item placement with WASD controls

### Inventory & Items
- 🎒 **Inventory System** - Separate inventories for singleplayer and multiplayer modes
- 🍎 **Collectible Items** - Apples, bananas, bamboo sapling, bamboo stacks, and wood stacks
- 🥤 **Manual Consumption** - Select consumable items and press space to consume (apples restore health, bananas reduce hunger)
- 📦 **Item Drops** - Trees drop resources when destroyed
- 🔄 **Network Sync** - Inventory synchronized across multiplayer sessions
- ⏱️ **Hunger Accumulation** - Hunger increases by 1% every 60 seconds; death occurs at 100% hunger

### Free World Mode
- 🎨 **Creative Exploration** - Activate Free World mode to receive 250 of each item type instantly
- 🚫 **No Save Restrictions** - All save functionality is disabled to prevent inventory persistence
- 🎮 **Perfect for Creativity** - Explore and build without resource constraints or survival pressure
- 👑 **Host-Only Activation** - In multiplayer, only the host can activate Free World for all players
- ⚠️ **Session-Only** - Free World state resets when you exit the game

### Multiplayer
- 🌐 **Dedicated Server** - Standalone server with configurable settings
- 👥 **Real-time Multiplayer** - Synchronized player positions, actions, and world state
- 📡 **Connection Quality Indicator** - Visual network status display
- 💾 **Separate Positions** - Independent player positions for singleplayer and multiplayer
- 🔌 **Disconnect/Reconnect** - Graceful connection handling with last server memory

### User Interface
- 📋 **In-Game Menu System** - Wooden plank themed menus with ESC key access
- 👤 **Player Name Customization** - Set custom player names (min 3 characters)
- 🖥️ **HUD Elements** - Health bar, inventory display, compass, and connection status
- 💾 **World Management** - Save, load, and manage multiple world saves
- 🎨 **Custom Fonts** - Retro pixel font (slkscr.ttf) for authentic game feel
- 🌍 **Multi-Language Support** - English, Polish (Polski), Portuguese (Português), German (Deutsch) and Dutch (Nederlands) with auto-detection

## Universal LPC Spritesheet Character Generator

[Universal LPC Spritesheet Character Generator](https://liberatedpixelcup.github.io/Universal-LPC-Spritesheet-Character-Generator/#?body=Body_color_amber&head=Human_male_amber&sex=male&nose=Straight_nose_amber&eyebrows=Thin_Eyebrows_chestnut&hair=Messy1_light_brown&clothes=Shortsleeve_Polo_navy&legs=Long_Pants_navy&shoes=Revised_Boots_black)
