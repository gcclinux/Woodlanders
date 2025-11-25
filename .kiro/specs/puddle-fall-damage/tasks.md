# Implementation Plan

- [x] 1. Extend WaterPuddle with unique identifier

  - Add UUID-based ID field to WaterPuddle class
  - Generate unique ID in reset() method when puddle spawns
  - Add getId() getter method
  - _Requirements: 3.1, 4.1_

- [x] 2. Create PuddleCollisionSystem class





  - [x] 2.1 Implement core collision detection logic


    - Create PuddleCollisionSystem class with collision checking
    - Implement calculateDistance() method using Euclidean distance
    - Implement checkCollision() method with 12-pixel radius check
    - Create PuddleCollisionResult data class
    - _Requirements: 1.1, 3.2, 3.3_

  - [x] 2.2 Write property test for collision detection


    - **Property 1: Collision triggers fall**
    - **Validates: Requirements 1.1, 3.2**

  - [x] 2.3 Implement triggered state tracking

    - Add Set<String> for triggered puddle IDs
    - Implement markPuddleTriggered() method
    - Implement isTriggered() check in collision detection
    - _Requirements: 4.1, 4.2_

  - [x] 2.4 Write property test for triggered state prevention


    - **Property 9: Triggered state prevents re-trigger**
    - **Validates: Requirements 4.1, 4.2**

  - [x] 2.5 Implement triggered state reset logic

    - Add Map<String, Boolean> for player-in-zone tracking
    - Implement updateTriggeredStates() method
    - Reset triggered state when player exits zone (distance > 12)
    - _Requirements: 4.3_

  - [x] 2.6 Write property test for triggered state reset


    - **Property 10: Triggered state reset on exit**
    - **Validates: Requirements 4.3**

  - [x] 2.7 Implement cleanup methods

    - Implement clearAllTriggeredStates() method
    - Clear both triggered set and in-zone map
    - _Requirements: 1.5, 3.4, 3.5, 4.4_

  - [x] 2.8 Write property test for cleanup


    - **Property 5: Cleanup on puddle despawn**
    - **Validates: Requirements 1.5, 3.4, 3.5, 4.4**

- [x] 3. Create FallAnimationSystem class



  - [x] 3.1 Implement animation state management

    - Create FallAnimationState enum (NONE, FALL, STANDUP_1-4, COMPLETE)
    - Create FallAnimationFrame data class with coordinates and duration
    - Initialize frame sequence with correct sprite coordinates
    - Add animation state tracking fields
    - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5_

  - [x] 3.2 Implement animation timing logic

    - Implement startFallSequence() method
    - Implement update() method with delta time accumulation
    - Advance frames every 0.8 seconds
    - Preserve remainder time for smooth transitions
    - _Requirements: 2.2, 2.3, 2.4, 2.5_

  - [x] 3.3 Write property test for animation timing

    - **Property 6: Animation sequence timing**
    - **Validates: Requirements 2.2, 2.3, 2.4, 2.5**

  - [x] 3.4 Implement frame extraction from sprite sheet


    - Implement getCurrentFallFrame() method
    - Extract 64x64 TextureRegion from sprite sheet
    - Use coordinates based on current animation state
    - Handle sprite sheet from PlayerConfig
    - _Requirements: 2.1, 2.7, 5.1, 5.2_

  - [x] 3.5 Write property test for sprite sheet consistency

    - **Property 7: Sprite sheet consistency**
    - **Validates: Requirements 2.7, 5.3**

  - [x] 3.6 Write property test for frame dimensions

    - **Property 12: Frame dimensions are correct**
    - **Validates: Requirements 5.1**

  - [x] 3.7 Implement state query methods

    - Implement isFallSequenceActive() method
    - Implement isFallSequenceComplete() method
    - Implement reset() method
    - _Requirements: 1.3, 1.4_

- [x] 4. Integrate collision and animation systems into Player class

  - [x] 4.1 Add new fields to Player class

    - Add PuddleCollisionSystem field
    - Add FallAnimationSystem field
    - Add boolean isFalling flag
    - Initialize systems in constructor
    - _Requirements: 1.1, 1.3_

  - [x] 4.2 Modify Player.update() for collision detection

    - Check puddle collision only when not falling
    - Get active puddles from PuddleManager
    - Call checkCollision() on collision system
    - Trigger fall if collision detected
    - _Requirements: 1.1, 3.2_

  - [x] 4.3 Implement triggerFall() method

    - Set isFalling flag to true
    - Start fall animation sequence
    - Mark puddle as triggered
    - Apply 10% damage to health
    - Clamp health to minimum 0
    - Send health update in multiplayer
    - _Requirements: 1.2, 4.1_

  - [x] 4.4 Write property test for fall damage

    - **Property 2: Fall damage is consistent**
    - **Validates: Requirements 1.2**

  - [x] 4.5 Modify Player.update() for fall animation

    - Update fall animation system with delta time
    - Check if sequence complete and call completeFall()
    - Skip normal movement processing while falling
    - Update triggered states after normal movement
    - _Requirements: 1.3, 1.4, 2.2-2.5_

  - [x] 4.6 Write property test for movement blocking

    - **Property 3: Movement blocked during fall**
    - **Validates: Requirements 1.3**

  - [x] 4.7 Write property test for fall sequence interruption prevention

    - **Property 11: Fall sequence prevents interruption**
    - **Validates: Requirements 4.5**

  - [x] 4.8 Implement completeFall() method

    - Set isFalling flag to false
    - Reset fall animation system
    - _Requirements: 1.4, 2.6_

  - [x] 4.9 Write property test for state restoration

    - **Property 4: State restoration after fall**
    - **Validates: Requirements 1.4, 2.6**

  - [x] 4.10 Modify Player.getCurrentFrame() for fall rendering

    - Check if falling and return fall animation frame
    - Otherwise return normal animation frame
    - _Requirements: 2.1, 5.4, 5.5_

  - [x] 4.11 Write property test for rendering position consistency

    - **Property 13: Rendering position consistency**
    - **Validates: Requirements 5.4**

  - [x] 4.12 Write property test for exclusive fall rendering

    - **Property 14: Exclusive fall rendering**
    - **Validates: Requirements 5.5**

- [x] 5. Extend PuddleManager to expose active puddles

  - [x] 5.1 Add getActivePuddles() method to PuddleManager

    - Iterate through puddle pool
    - Collect active puddles into list
    - Return list for collision detection
    - _Requirements: 1.1, 3.1_

  - [x] 5.2 Write property test for puddle coordinates

    - **Property 8: Puddle coordinates recorded**
    - **Validates: Requirements 3.1**

  - [x] 5.3 Modify PuddleManager cleanup to notify collision system

    - Call clearAllTriggeredStates() when clearing puddles
    - Call clearAllTriggeredStates() on state transition to NONE
    - Pass collision system reference to manager
    - _Requirements: 1.5, 3.4, 3.5, 4.4_

- [x] 6. Add error handling and validation

  - [x] 6.1 Add position validation in collision detection

    - Check for NaN and Infinity in player/puddle positions
    - Log warning and skip invalid positions
    - Return no collision for invalid data
    - _Error Handling: Collision Detection Errors_

  - [x] 6.2 Add null checks for puddle list

    - Check for null/empty puddle list before iteration
    - Return no collision result for null list
    - _Error Handling: Collision Detection Errors_

  - [x] 6.3 Add sprite sheet error handling

    - Catch texture loading exceptions in FallAnimationSystem
    - Fall back to default sprite sheet on error
    - Log error for debugging
    - _Error Handling: Animation System Errors_

  - [x] 6.4 Add animation timeout protection

    - Implement 5-second maximum duration for fall sequence
    - Force completion if timeout reached
    - Reset fall state to prevent stuck player
    - Log warning for debugging
    - _Error Handling: State Management Errors_

  - [x] 6.5 Add triggered set size limit

    - Implement maximum size limit for triggered puddle set
    - Remove oldest entries if limit exceeded
    - Clear on puddle count changes
    - _Error Handling: State Management Errors_

- [x] 7. Checkpoint - Ensure all tests pass

  - Ensure all tests pass, ask the user if questions arise.

- [x] 8. Add multiplayer synchronization

  - [x] 8.1 Create PlayerFallMessage network message


    - Add message class with player ID and puddle ID
    - Register message type in network system
    - _Multiplayer Considerations_

  - [x] 8.2 Implement client-side fall event sending


    - Send PlayerFallMessage to server on fall trigger
    - Include puddle ID for validation
    - _Multiplayer Considerations_

  - [x] 8.3 Implement server-side fall validation


    - Validate puddle exists and player position
    - Broadcast fall event to all clients
    - _Multiplayer Considerations_

  - [x] 8.4 Implement remote player fall animation


    - Receive fall event for remote players
    - Trigger fall animation on remote player instances
    - _Multiplayer Considerations_

- [x] 9. Add integration tests

  - [x] 9.1 Write puddle-player integration test

    - Spawn puddles in test environment
    - Move player through puddles
    - Verify fall triggers and damage applies
    - Verify animation plays and completes
    - _Integration Testing_

  - [x] 9.2 Write animation-rendering integration test


    - Trigger fall sequence
    - Capture rendered frames at each time step
    - Verify correct sprite coordinates
    - Verify timing between frames
    - _Integration Testing_

  - [x] 9.3 Write cleanup integration test

    - Create puddles and trigger falls
    - Transition puddle system to NONE
    - Verify all collision data cleared
    - Check for memory leaks
    - _Integration Testing_

- [x] 10. Add performance tests

  - [x] 10.1 Write collision detection performance test

    - Test with maximum puddle count (8)
    - Measure collision check time per frame
    - Verify < 1ms per frame target
    - _Performance Testing_

  - [x] 10.2 Write animation system performance test

    - Test fall sequence with multiple players
    - Measure frame extraction and rendering time
    - Verify no frame rate impact
    - _Performance Testing_

  - [x] 10.3 Write memory usage test

    - Monitor triggered puddle set size over time
    - Verify cleanup occurs properly
    - Verify no memory growth
    - _Performance Testing_
