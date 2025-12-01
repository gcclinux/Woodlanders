# Implementation Plan

- [x] 1. Add sound fields to BirdFormationManager

  - Add `private Sound birdSound;` field to store the bird sound asset
  - Add `private long birdSoundId = -1;` field to track sound playback instance
  - Initialize `birdSoundId` to -1 to indicate no sound is playing
  - _Requirements: 1.2, 3.3_

- [x] 2. Implement sound loading in initialize() method


  - Add try-catch block to handle sound loading errors gracefully
  - Load bird sound using `Gdx.audio.newSound(Gdx.files.internal("sound/birds.wav"))`
  - Set `birdSound` to null if loading fails
  - Add logging for successful load and error cases
  - _Requirements: 3.1, 4.1, 4.2_

- [x] 2.1 Write property test for null sound safety


  - **Property 5: Null sound safety**
  - **Validates: Requirements 4.2, 4.3**

- [x] 3. Implement sound start in spawnFormation() method


  - Add null check for `birdSound` before attempting to play
  - Check that `birdSoundId == -1` to ensure sound isn't already playing
  - Start looping sound using `birdSoundId = birdSound.loop()`
  - Add logging for sound start event
  - _Requirements: 1.1, 1.2, 2.1_

- [x] 3.1 Write property test for sound starts on spawn


  - **Property 1: Sound starts on spawn**
  - **Validates: Requirements 1.1, 1.2**

- [x] 4. Implement sound stop in despawnFormation() method


  - Add null check for `birdSound` before attempting to stop
  - Check that `birdSoundId != -1` to ensure sound is actually playing
  - Stop sound using `birdSound.stop(birdSoundId)`
  - Reset `birdSoundId` to -1 after stopping
  - Add logging for sound stop event
  - _Requirements: 1.4, 1.5, 2.2_

- [x] 4.1 Write property test for sound stops on despawn


  - **Property 2: Sound stops on despawn**
  - **Validates: Requirements 1.4, 1.5**

- [x] 4.2 Write property test for sound state matches formation state


  - **Property 3: Sound state matches formation state**
  - **Validates: Requirements 1.3, 2.4**

- [x] 4.3 Write property test for spawn-despawn cycle correctness


  - **Property 4: Spawn-despawn cycle correctness**
  - **Validates: Requirements 2.1, 2.2, 2.3**

- [x] 5. Implement sound cleanup in dispose() method


  - Stop sound if currently playing (check `birdSoundId != -1`)
  - Reset `birdSoundId` to -1 after stopping
  - Dispose Sound object if not null
  - Set `birdSound` to null after disposal
  - Ensure dispose is safe to call multiple times
  - _Requirements: 3.4, 5.1, 5.2, 5.4_

- [x] 5.1 Write property test for disposal cleanup


  - **Property 6: Disposal cleanup**
  - **Validates: Requirements 3.4, 5.1, 5.2**

- [x] 5.2 Write property test for idempotent disposal


  - **Property 7: Idempotent disposal**
  - **Validates: Requirements 5.4**

- [x] 6. Add error handling for sound operations


  - Wrap sound.loop() in try-catch to handle playback errors
  - Wrap sound.stop() in try-catch to handle stop errors
  - Log errors and continue execution without crashing
  - Ensure null checks prevent NullPointerExceptions
  - _Requirements: 4.3, 4.4_

- [x] 6.1 Write property test for error handling resilience


  - **Property 8: Error handling resilience**
  - **Validates: Requirements 4.1, 4.4**

- [x] 7. Verify sound file exists


  - Confirm `assets/sound/birds.wav` file exists in the project
  - Test that the file can be loaded by LibGDX
  - Verify audio format is compatible (WAV format)
  - _Requirements: 3.1_

- [x] 8. Final checkpoint - Ensure all tests pass



  - Ensure all tests pass, ask the user if questions arise.
