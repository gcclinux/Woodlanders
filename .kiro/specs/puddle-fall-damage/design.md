# Design Document

## Overview

The puddle fall damage system extends the existing weather/puddle mechanics by adding player interaction. When a player walks over a puddle, they trigger a fall sequence that includes damage, animation, and temporary movement restriction. The system integrates with the existing `PuddleManager`, `WaterPuddle`, and `Player` classes to create an environmental hazard that adds challenge and visual feedback to rainy weather.

The design follows a state-based approach where the player can be in either a normal state or a falling state. The falling state progresses through a 5-frame animation sequence with precise timing before returning to normal gameplay.

## Architecture

### Component Interaction

```
┌─────────────────┐
│  PuddleManager  │
│                 │
│  - Manages      │
│    puddle       │
│    lifecycle    │
└────────┬────────┘
         │
         │ provides puddle list
         ▼
┌─────────────────┐      ┌──────────────────┐
│ PuddleCollision │◄─────┤     Player       │
│     System      │      │                  │
│                 │      │  - Position      │
│  - Detects      │      │  - Health        │
│    intersections│      │  - Animation     │
│  - Tracks       │      │  - Movement      │
│    triggered    │      └──────────────────┘
│    puddles      │
└─────────────────┘
         │
         │ triggers fall
         ▼
┌─────────────────┐
│  FallAnimation  │
│     System      │
│                 │
│  - Manages      │
│    animation    │
│    sequence     │
│  - Timing       │
│  - Sprite       │
│    selection    │
└─────────────────┘
```

### Data Flow

1. **Puddle Spawning**: `PuddleManager` creates `WaterPuddle` instances with position data
2. **Collision Detection**: Each frame, `Player` checks its position against active puddles
3. **Fall Trigger**: When collision detected and puddle not triggered, initiate fall sequence
4. **Animation Playback**: `FallAnimationSystem` progresses through 5 frames with 0.8s timing
5. **State Restoration**: After final frame, player returns to normal movement and rendering

## Components and Interfaces

### 1. PuddleCollisionSystem

**Purpose**: Manages collision detection between player and puddles, tracks triggered states.

**Key Methods**:
```java
public class PuddleCollisionSystem {
    // Check if player is within fall zone of any puddle
    public PuddleCollisionResult checkCollision(float playerX, float playerY, 
                                                List<WaterPuddle> activePuddles);
    
    // Mark a puddle as triggered for the player
    public void markPuddleTriggered(WaterPuddle puddle);
    
    // Reset triggered state when player exits puddle zone
    public void updateTriggeredStates(float playerX, float playerY, 
                                      List<WaterPuddle> activePuddles);
    
    // Clear all triggered states (called when puddles despawn)
    public void clearAllTriggeredStates();
    
    // Calculate distance from player center to puddle center
    private float calculateDistance(float playerX, float playerY, WaterPuddle puddle);
}
```

**Data Structures**:
- `Set<String> triggeredPuddleIds`: Tracks which puddles have been triggered
- `Map<String, Boolean> playerInZone`: Tracks if player is currently in each puddle's zone

### 2. FallAnimationSystem

**Purpose**: Manages the fall-and-standup animation sequence with precise timing.

**Key Methods**:
```java
public class FallAnimationSystem {
    // Start the fall animation sequence
    public void startFallSequence();
    
    // Update animation state based on elapsed time
    public void update(float deltaTime);
    
    // Get current frame to render
    public TextureRegion getCurrentFallFrame();
    
    // Check if fall sequence is active
    public boolean isFallSequenceActive();
    
    // Check if fall sequence is complete
    public boolean isFallSequenceComplete();
    
    // Reset the animation system
    public void reset();
}
```

**Animation States**:
```java
public enum FallAnimationState {
    NONE,           // Not in fall sequence
    FALL,           // Frame 1: (256, 1280)
    STANDUP_1,      // Frame 2: (192, 1280)
    STANDUP_2,      // Frame 3: (128, 1280)
    STANDUP_3,      // Frame 4: (64, 1280)
    STANDUP_4,      // Frame 5: (0, 1280)
    COMPLETE        // Sequence finished
}
```

### 3. Player Integration

**Modifications to Player class**:

```java
public class Player {
    // New fields
    private PuddleCollisionSystem puddleCollisionSystem;
    private FallAnimationSystem fallAnimationSystem;
    private boolean isFalling;
    
    // Modified update method
    public void update(float deltaTime) {
        // Check for puddle collision (only if not already falling)
        if (!isFalling && puddleManager != null) {
            PuddleCollisionResult collision = puddleCollisionSystem.checkCollision(
                x, y, puddleManager.getActivePuddles()
            );
            
            if (collision.hasCollision()) {
                triggerFall(collision.getPuddle());
            }
        }
        
        // Update fall animation if active
        if (isFalling) {
            fallAnimationSystem.update(deltaTime);
            
            if (fallAnimationSystem.isFallSequenceComplete()) {
                completeFall();
            }
            
            // Skip normal movement processing while falling
            return;
        }
        
        // Normal movement and input processing...
        // (existing code)
        
        // Update triggered puddle states
        if (puddleManager != null) {
            puddleCollisionSystem.updateTriggeredStates(x, y, 
                puddleManager.getActivePuddles());
        }
    }
    
    // Modified getCurrentFrame method
    public TextureRegion getCurrentFrame() {
        if (isFalling) {
            return fallAnimationSystem.getCurrentFallFrame();
        }
        
        // Normal animation logic...
        // (existing code)
    }
    
    private void triggerFall(WaterPuddle puddle) {
        isFalling = true;
        fallAnimationSystem.startFallSequence();
        puddleCollisionSystem.markPuddleTriggered(puddle);
        
        // Apply 10% damage
        float damage = 10.0f;
        health = Math.max(0, health - damage);
        
        // Send health update in multiplayer
        checkAndSendHealthUpdate();
    }
    
    private void completeFall() {
        isFalling = false;
        fallAnimationSystem.reset();
    }
}
```

### 4. WaterPuddle Extension

**Add unique identifier for tracking**:

```java
public class WaterPuddle {
    private String id; // Unique identifier for triggered state tracking
    
    // Generate ID when puddle is reset/spawned
    public void reset(float x, float y, float width, float height, float rotation) {
        this.id = UUID.randomUUID().toString();
        // ... existing reset logic
    }
    
    public String getId() {
        return id;
    }
}
```

### 5. PuddleManager Extension

**Add method to expose active puddles**:

```java
public class PuddleManager {
    // Get list of currently active puddles for collision detection
    public List<WaterPuddle> getActivePuddles() {
        List<WaterPuddle> active = new ArrayList<>();
        for (WaterPuddle puddle : puddleRenderer.getPuddlePool()) {
            if (puddle.isActive()) {
                active.add(puddle);
            }
        }
        return active;
    }
}
```

## Data Models

### PuddleCollisionResult

```java
public class PuddleCollisionResult {
    private boolean hasCollision;
    private WaterPuddle puddle;
    
    public PuddleCollisionResult(boolean hasCollision, WaterPuddle puddle) {
        this.hasCollision = hasCollision;
        this.puddle = puddle;
    }
    
    public boolean hasCollision() {
        return hasCollision;
    }
    
    public WaterPuddle getPuddle() {
        return puddle;
    }
}
```

### FallAnimationFrame

```java
public class FallAnimationFrame {
    private int spriteX;
    private int spriteY;
    private float duration;
    
    public FallAnimationFrame(int spriteX, int spriteY, float duration) {
        this.spriteX = spriteX;
        this.spriteY = spriteY;
        this.duration = duration;
    }
    
    // Getters
    public int getSpriteX() { return spriteX; }
    public int getSpriteY() { return spriteY; }
    public float getDuration() { return duration; }
}
```


## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system-essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: Collision triggers fall

*For any* player position and puddle position, when the distance from player center to puddle center is less than or equal to 12 pixels and the puddle is not triggered, the fall mechanic should activate.

**Validates: Requirements 1.1, 3.2**

### Property 2: Fall damage is consistent

*For any* initial player health value, when a fall is triggered, the player's health should decrease by exactly 10%.

**Validates: Requirements 1.2**

### Property 3: Movement blocked during fall

*For any* movement input during an active fall sequence, the player's position should remain unchanged until the sequence completes.

**Validates: Requirements 1.3**

### Property 4: State restoration after fall

*For any* completed fall sequence, the player should return to normal movement capability and normal directional sprite rendering.

**Validates: Requirements 1.4, 2.6**

### Property 5: Cleanup on puddle despawn

*For any* puddle system state transition to NONE or evaporation completion, all puddle collision data and triggered states should be cleared from memory.

**Validates: Requirements 1.5, 3.4, 3.5, 4.4**

### Property 6: Animation sequence timing

*For any* fall sequence, the animation should progress through all 5 frames (fall, standup1, standup2, standup3, standup4) with exactly 0.8 seconds between each frame transition.

**Validates: Requirements 2.2, 2.3, 2.4, 2.5**

### Property 7: Sprite sheet consistency

*For any* PlayerConfig sprite sheet selection, the fall and standup frames should be extracted from the same sprite sheet used for normal player animations.

**Validates: Requirements 2.7, 5.3**

### Property 8: Puddle coordinates recorded

*For any* spawned puddle, the system should record and maintain its center coordinates for collision detection.

**Validates: Requirements 3.1**

### Property 9: Triggered state prevents re-trigger

*For any* puddle marked as triggered, positioning the player within its fall zone should not trigger another fall sequence.

**Validates: Requirements 4.1, 4.2**

### Property 10: Triggered state reset on exit

*For any* triggered puddle, when the player moves outside its fall zone (distance > 12 pixels), the triggered state should be reset.

**Validates: Requirements 4.3**

### Property 11: Fall sequence prevents interruption

*For any* active fall sequence, collision checks with other puddles should be ignored until the sequence completes.

**Validates: Requirements 4.5**

### Property 12: Frame dimensions are correct

*For any* fall or standup frame extraction, the texture region should be exactly 64x64 pixels.

**Validates: Requirements 5.1**

### Property 13: Rendering position consistency

*For any* fall/standup frame rendering, the sprite should be rendered at the same screen position as normal player sprites.

**Validates: Requirements 5.4**

### Property 14: Exclusive fall rendering

*For any* active fall sequence, normal walking and idle animations should not be rendered.

**Validates: Requirements 5.5**

## Error Handling

### Collision Detection Errors

**Scenario**: Player position or puddle position is invalid (NaN, Infinity)
**Handling**: 
- Validate positions before distance calculation
- Log warning and skip collision check for invalid positions
- Continue normal gameplay without crash

**Scenario**: Puddle list is null or empty
**Handling**:
- Check for null/empty before iteration
- Return no collision result
- Continue normal gameplay

### Animation System Errors

**Scenario**: Sprite sheet is missing or corrupted
**Handling**:
- Catch texture loading exceptions
- Fall back to default sprite sheet (boy_navy_start.png)
- Log error for debugging
- Continue with fallback sprites

**Scenario**: Fall animation frame coordinates are out of bounds
**Handling**:
- Validate coordinates before texture region extraction
- Use default idle frame if coordinates invalid
- Log error for debugging
- Complete fall sequence early to restore normal state

### State Management Errors

**Scenario**: Fall sequence timer exceeds expected duration
**Handling**:
- Implement maximum duration timeout (5 seconds)
- Force completion if timeout reached
- Reset fall state to prevent player getting stuck
- Log warning for debugging

**Scenario**: Triggered puddle set grows unbounded
**Handling**:
- Clear triggered states when puddle count changes
- Implement maximum set size limit
- Remove oldest entries if limit exceeded

### Multiplayer Synchronization

**Scenario**: Fall state not synchronized between client and server
**Handling**:
- Send fall event message to server when triggered
- Server validates fall and broadcasts to other clients
- Client applies damage locally for responsiveness
- Server corrects health if mismatch detected

## Testing Strategy

### Unit Testing

The testing strategy employs both unit tests and property-based tests to ensure comprehensive coverage:

**Unit tests** verify specific examples and edge cases:
- Exact boundary condition: player at exactly 12 pixels from puddle center
- Zero health edge case: player with 10% health falling (should not go negative)
- Empty puddle list: collision check with no active puddles
- Null sprite sheet: fall animation with missing texture
- State transitions: NONE → ACCUMULATING → ACTIVE → EVAPORATING → NONE

**Property-based tests** verify universal properties across all inputs:
- All collision detection scenarios with random positions
- All health values and damage calculations
- All animation timing variations
- All sprite sheet configurations

### Property-Based Testing

**Framework**: We will use **jqwik** for property-based testing in Java. Jqwik is a mature PBT library that integrates well with JUnit 5 and provides powerful generators for complex data types.

**Configuration**: Each property-based test will run a minimum of 100 iterations to ensure adequate coverage of the input space.

**Test Tagging**: Each property-based test must be tagged with a comment explicitly referencing the correctness property from this design document using the format: `**Feature: puddle-fall-damage, Property {number}: {property_text}**`

**Example Test Structure**:
```java
@Property
void collisionTriggersFall(@ForAll @FloatRange(min = 0, max = 1000) float playerX,
                           @ForAll @FloatRange(min = 0, max = 1000) float playerY,
                           @ForAll @FloatRange(min = 0, max = 1000) float puddleX,
                           @ForAll @FloatRange(min = 0, max = 1000) float puddleY) {
    // **Feature: puddle-fall-damage, Property 1: Collision triggers fall**
    
    // Calculate distance
    float distance = calculateDistance(playerX, playerY, puddleX, puddleY);
    
    // Create collision system and check
    PuddleCollisionSystem system = new PuddleCollisionSystem();
    WaterPuddle puddle = createPuddle(puddleX, puddleY);
    PuddleCollisionResult result = system.checkCollision(playerX, playerY, 
                                                         List.of(puddle));
    
    // Verify property
    if (distance <= 12.0f) {
        assertThat(result.hasCollision()).isTrue();
    } else {
        assertThat(result.hasCollision()).isFalse();
    }
}
```

### Integration Testing

**Puddle-Player Integration**:
- Spawn puddles in test environment
- Move player through puddles
- Verify fall triggers, damage applies, animation plays
- Verify state restoration after completion

**Animation-Rendering Integration**:
- Trigger fall sequence
- Capture rendered frames at each time step
- Verify correct sprite coordinates used
- Verify timing between frames

**Cleanup Integration**:
- Create puddles and trigger falls
- Transition puddle system to NONE state
- Verify all collision data cleared
- Verify no memory leaks

### Performance Testing

**Collision Detection Performance**:
- Test with maximum puddle count (8 puddles)
- Measure collision check time per frame
- Target: < 1ms per frame for collision checks

**Animation System Performance**:
- Test fall sequence with multiple players
- Measure frame extraction and rendering time
- Target: No noticeable frame rate impact

**Memory Usage**:
- Monitor triggered puddle set size
- Verify cleanup occurs properly
- Target: No memory growth over time

## Implementation Notes

### Coordinate System

The sprite sheet uses a top-left origin (0,0) at the top of the image. LibGDX's TextureRegion also uses this convention. The fall/standup sprites are located at Y=1280 (from the top) in the sprite sheet:

- Fall: X=256, Y=1280
- Standup1: X=192, Y=1280
- Standup2: X=128, Y=1280
- Standup3: X=64, Y=1280
- Standup4: X=0, Y=1280

All frames are 64x64 pixels.

### Timing Precision

The 0.8-second timing between frames should be implemented using accumulated delta time rather than fixed delays to ensure smooth animation regardless of frame rate variations:

```java
private float animationTimer = 0.0f;
private static final float FRAME_DURATION = 0.8f;

public void update(float deltaTime) {
    animationTimer += deltaTime;
    
    if (animationTimer >= FRAME_DURATION) {
        advanceToNextFrame();
        animationTimer -= FRAME_DURATION; // Preserve remainder
    }
}
```

### Collision Detection Optimization

To avoid checking every puddle every frame, implement early exit when fall is triggered:

```java
public PuddleCollisionResult checkCollision(float playerX, float playerY, 
                                           List<WaterPuddle> puddles) {
    for (WaterPuddle puddle : puddles) {
        if (isTriggered(puddle)) {
            continue; // Skip triggered puddles
        }
        
        float distance = calculateDistance(playerX, playerY, puddle);
        if (distance <= FALL_ZONE_RADIUS) {
            return new PuddleCollisionResult(true, puddle);
        }
    }
    
    return new PuddleCollisionResult(false, null);
}
```

### Multiplayer Considerations

In multiplayer mode, fall events should be synchronized:

1. Client detects collision and triggers fall locally
2. Client sends `PlayerFallMessage` to server with puddle ID
3. Server validates fall (checks puddle exists, player position)
4. Server broadcasts fall event to all clients
5. Other clients render fall animation for remote player

This ensures all players see consistent fall animations while maintaining responsive local gameplay.
