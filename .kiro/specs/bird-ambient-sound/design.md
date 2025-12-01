# Design Document

## Overview

The bird ambient sound system adds audio atmosphere to the existing flying birds feature by playing bird chirping sounds when bird formations are visible on screen. The system follows the same architectural pattern as the rain sound implementation in `RainSystem`, using LibGDX's Sound API to manage audio playback. The sound plays on loop when birds spawn and stops when they despawn, creating a synchronized audio-visual experience.

## Architecture

The sound system integrates directly into the existing `BirdFormationManager` class, following the same pattern used in `RainSystem`:

```
BirdFormationManager
    ├── Sound birdSound (audio asset)
    ├── long birdSoundId (playback instance identifier)
    ├── initialize() - loads bird sound asset
    ├── update() - manages sound playback based on formation state
    │   ├── Start sound when formation spawns
    │   └── Stop sound when formation despawns
    └── dispose() - cleans up sound resources
```

### Integration Points

- **BirdFormationManager.initialize()**: Load `assets/sound/birds.wav` using LibGDX Sound API
- **BirdFormationManager.spawnFormation()**: Start looping bird sound, store sound instance ID
- **BirdFormationManager.despawnFormation()**: Stop bird sound, reset sound instance ID
- **BirdFormationManager.dispose()**: Stop any playing sound and dispose Sound object

## Components and Interfaces

### BirdFormationManager (Enhanced)

The existing `BirdFormationManager` class will be enhanced with sound management capabilities.

**New Fields:**
```java
private Sound birdSound;           // The bird sound asset
private long birdSoundId = -1;     // Sound instance ID (-1 = not playing)
```

**Modified Methods:**
```java
public void initialize() {
    // Existing bird texture loading...
    
    // Load bird sound asset
    try {
        this.birdSound = Gdx.audio.newSound(Gdx.files.internal("sound/birds.wav"));
        System.out.println("[BIRDS] Bird sound loaded successfully");
    } catch (Exception e) {
        System.err.println("[BIRDS] Failed to load bird sound: " + e.getMessage());
        this.birdSound = null;
    }
}

private void spawnFormation() {
    // Existing spawn logic...
    
    // Start bird sound
    if (birdSound != null && birdSoundId == -1) {
        birdSoundId = birdSound.loop();
        System.out.println("[BIRDS] Bird sound started");
    }
}

private void despawnFormation() {
    // Stop bird sound
    if (birdSound != null && birdSoundId != -1) {
        birdSound.stop(birdSoundId);
        birdSoundId = -1;
        System.out.println("[BIRDS] Bird sound stopped");
    }
    
    // Existing despawn logic...
}

public void dispose() {
    // Stop sound if playing
    if (birdSound != null && birdSoundId != -1) {
        birdSound.stop(birdSoundId);
        birdSoundId = -1;
    }
    
    // Dispose sound resource
    if (birdSound != null) {
        birdSound.dispose();
        birdSound = null;
    }
    
    // Existing disposal logic...
}
```

## Data Models

### Sound State Model

The sound system maintains a simple state machine:

```
NOT_LOADED (birdSound == null)
    └── Cannot play sound, all operations skipped

LOADED_STOPPED (birdSound != null, birdSoundId == -1)
    └── Ready to play, no sound currently playing

LOADED_PLAYING (birdSound != null, birdSoundId != -1)
    └── Sound is actively playing, can be stopped
```

### State Transitions

```
initialize() with success:  NOT_LOADED → LOADED_STOPPED
initialize() with failure:  NOT_LOADED → NOT_LOADED
spawnFormation():          LOADED_STOPPED → LOADED_PLAYING
despawnFormation():        LOADED_PLAYING → LOADED_STOPPED
dispose():                 ANY_STATE → NOT_LOADED
```

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system-essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: Sound starts on spawn

*For any* bird formation spawn event, if the sound is loaded, the sound instance ID should be set to a valid value (not -1) immediately after spawning
**Validates: Requirements 1.1, 1.2**

### Property 2: Sound stops on despawn

*For any* bird formation despawn event, if the sound was playing, the sound instance ID should be reset to -1 immediately after despawning
**Validates: Requirements 1.4, 1.5**

### Property 3: Sound state matches formation state

*For any* point in time during game execution, the sound should be playing (birdSoundId != -1) if and only if a bird formation is active (activeFormation != null)
**Validates: Requirements 1.3, 2.4**

### Property 4: Spawn-despawn cycle correctness

*For any* sequence of spawn and despawn operations, the sound state should correctly transition between playing and stopped states matching the formation lifecycle
**Validates: Requirements 2.1, 2.2, 2.3**

### Property 5: Null sound safety

*For any* operation (spawn, despawn, update, dispose), if the sound object is null, no exceptions should be thrown and the system should continue normally
**Validates: Requirements 4.2, 4.3**

### Property 6: Disposal cleanup

*For any* call to dispose(), if the sound was playing, it should be stopped, and the Sound object should be disposed
**Validates: Requirements 3.4, 5.1, 5.2**

### Property 7: Idempotent disposal

*For any* state (sound playing or stopped), calling dispose() should safely clean up resources without throwing exceptions
**Validates: Requirements 5.4**

### Property 8: Error handling resilience

*For any* sound loading failure or playback error, the system should log the error and continue execution without crashing
**Validates: Requirements 4.1, 4.4**

## Error Handling

### Sound Loading Failures

- **Issue**: `birds.wav` file missing or corrupted
- **Handling**: Catch exception during `Gdx.audio.newSound()`, log error, set `birdSound` to null
- **Recovery**: System continues without sound, birds still render normally
- **User Impact**: Visual birds without audio (graceful degradation)

### Sound Playback Failures

- **Issue**: Exception during `sound.loop()` or `sound.stop()`
- **Handling**: Wrap sound operations in try-catch, log error, reset sound state
- **Recovery**: Attempt to play sound on next spawn cycle
- **User Impact**: Temporary audio glitch, system remains stable

### Null Sound Handling

- **Issue**: Sound object is null (failed to load)
- **Handling**: Check `birdSound != null` before all sound operations
- **Recovery**: Skip sound operations, continue with visual-only birds
- **User Impact**: No audio, but game functions normally

### Disposal Safety

- **Issue**: Disposing already-disposed sound or stopping already-stopped sound
- **Handling**: Check state before operations, use defensive programming
- **Recovery**: No recovery needed, operations are idempotent
- **User Impact**: None, cleanup is safe

## Testing Strategy

### Unit Tests

- Test sound loading with valid `birds.wav` file
- Test sound loading with missing file (error handling)
- Test spawn starts sound (verify soundId != -1)
- Test despawn stops sound (verify soundId == -1)
- Test dispose stops sound and cleans up resources
- Test null sound handling (no exceptions thrown)
- Test multiple spawn-despawn cycles

### Property-Based Tests

Property-based tests will use Java's jqwik library for property testing. Each test will run a minimum of 100 iterations with randomly generated inputs.

- **Property 1**: Generate random spawn events, verify sound ID is valid after each spawn
- **Property 2**: Generate random despawn events, verify sound ID is -1 after each despawn
- **Property 3**: Generate random game states, verify sound state matches formation state
- **Property 4**: Generate random sequences of spawn/despawn, verify correct state transitions
- **Property 5**: Test all operations with null sound, verify no exceptions
- **Property 6**: Test disposal in various states, verify cleanup occurs
- **Property 7**: Test multiple dispose calls, verify idempotency
- **Property 8**: Test error conditions, verify system continues without crashing

### Integration Tests

- Test bird sound integration with full game loop
- Test sound plays when birds spawn during gameplay
- Test sound stops when birds despawn during gameplay
- Test sound system with rapid spawn-despawn cycles
- Test sound cleanup on game shutdown
- Test sound behavior with multiple bird spawn cycles over time

## Implementation Notes

### Pattern Consistency with RainSystem

The implementation follows the exact same pattern as `RainSystem.java`:

1. **Sound field**: `private Sound birdSound;`
2. **Sound ID field**: `private long birdSoundId = -1;`
3. **Loading**: In `initialize()`, use `Gdx.audio.newSound(Gdx.files.internal("sound/birds.wav"))`
4. **Starting**: Use `birdSoundId = birdSound.loop();` when formation spawns
5. **Stopping**: Use `birdSound.stop(birdSoundId); birdSoundId = -1;` when formation despawns
6. **Disposal**: Stop sound if playing, then `birdSound.dispose()`

### Sound File Location

The bird sound file is located at `assets/sound/birds.wav` (already exists in the project). The file path in code is `"sound/birds.wav"` (LibGDX automatically looks in the assets folder).

### Performance Considerations

- **Memory**: Single Sound object loaded once, minimal memory footprint
- **CPU**: Sound operations are lightweight, no performance impact
- **Audio Mixing**: LibGDX handles mixing bird sound with rain sound automatically
- **Looping**: Using `sound.loop()` is efficient, no manual loop management needed

### Sound Instance ID Management

- **Initial State**: `birdSoundId = -1` indicates no sound playing
- **Playing State**: `birdSoundId` contains valid ID returned by `sound.loop()`
- **Stopped State**: Reset to `-1` after calling `sound.stop(birdSoundId)`
- **Checking**: Always verify `birdSoundId != -1` before calling `stop()`

### Defensive Programming

All sound operations include null checks:
```java
if (birdSound != null && birdSoundId == -1) {
    birdSoundId = birdSound.loop();
}
```

This ensures the system never crashes due to sound issues and gracefully degrades to visual-only birds if audio fails.

### Logging Strategy

- Log successful sound loading: `"[BIRDS] Bird sound loaded successfully"`
- Log sound loading failure: `"[BIRDS] Failed to load bird sound: {error}"`
- Log sound start: `"[BIRDS] Bird sound started"` (optional, for debugging)
- Log sound stop: `"[BIRDS] Bird sound stopped"` (optional, for debugging)

### No Volume Control Needed

Unlike rain (which adjusts volume based on intensity), bird sound plays at constant volume since bird formations are either present or absent (binary state). If future requirements need volume control, it can be added using `birdSound.setVolume(birdSoundId, volume)`.
