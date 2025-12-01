package wagemaker.uk.birds;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Property-based test for spawn-despawn cycle correctness.
 * Feature: bird-ambient-sound, Property 4: Spawn-despawn cycle correctness
 * Validates: Requirements 2.1, 2.2, 2.3
 */
public class SpawnDespawnCycleCorrectnessPropertyTest {
    
    private static HeadlessApplication application;
    
    @BeforeAll
    public static void setupGdx() {
        HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
        application = new HeadlessApplication(new ApplicationAdapter() {
            @Override
            public void create() {
                Gdx.gl = Mockito.mock(GL20.class);
            }
        }, config);
        
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    @AfterAll
    public static void tearDownGdx() {
        if (application != null) {
            application.exit();
        }
    }
    
    /**
     * Property 4: Spawn-despawn cycle correctness
     * For any sequence of spawn and despawn operations, the sound state should correctly
     * transition between playing and stopped states matching the formation lifecycle.
     * Validates: Requirements 2.1, 2.2, 2.3
     * 
     * This property-based test runs 100 trials and verifies that:
     * 1. Sound transitions from stopped to playing on spawn
     * 2. Sound transitions from playing to stopped on despawn
     * 3. Multiple cycles maintain correct state transitions
     * 4. No state corruption occurs across cycles
     */
    @Test
    public void spawnDespawnCycleCorrectness() throws Exception {
        float viewWidth = 800f;
        float viewHeight = 600f;
        
        OrthographicCamera camera = new OrthographicCamera();
        camera.setToOrtho(false, viewWidth, viewHeight);
        Viewport viewport = new FitViewport(viewWidth, viewHeight, camera);
        
        // Run 100 trials
        for (int trial = 0; trial < 100; trial++) {
            Sound mockSound = Mockito.mock(Sound.class);
            long mockSoundId = 8000L + trial;
            when(mockSound.loop()).thenReturn(mockSoundId);
            
            com.badlogic.gdx.graphics.Texture mockTexture = Mockito.mock(com.badlogic.gdx.graphics.Texture.class);
            BirdFormationManager manager = new BirdFormationManager(camera, viewport, mockTexture);
            
            // Inject mock sound
            Field soundField = BirdFormationManager.class.getDeclaredField("birdSound");
            soundField.setAccessible(true);
            soundField.set(manager, mockSound);
            
            Field soundIdField = BirdFormationManager.class.getDeclaredField("birdSoundId");
            soundIdField.setAccessible(true);
            
            // Initial state: stopped
            assertEquals(
                -1L,
                soundIdField.getLong(manager),
                "Trial " + trial + ": Initial state should be stopped (soundId == -1)"
            );
            
            // Perform spawn-despawn cycle
            Field nextSpawnIntervalField = BirdFormationManager.class.getDeclaredField("nextSpawnInterval");
            nextSpawnIntervalField.setAccessible(true);
            float interval = nextSpawnIntervalField.getFloat(manager);
            
            // Transition: stopped -> playing (spawn)
            manager.update(interval + 0.1f, 0, 0);
            
            long afterSpawnId = soundIdField.getLong(manager);
            assertEquals(
                mockSoundId,
                afterSpawnId,
                "Trial " + trial + ": After spawn, sound should transition to playing"
            );
            
            // Wait for despawn
            float maxUpdateTime = 20f;
            float totalTime = 0f;
            float timeStep = 0.1f;
            
            while (manager.getActiveFormation() != null && totalTime < maxUpdateTime) {
                manager.update(timeStep, 0, 0);
                totalTime += timeStep;
            }
            
            // Transition: playing -> stopped (despawn)
            long afterDespawnId = soundIdField.getLong(manager);
            assertEquals(
                -1L,
                afterDespawnId,
                "Trial " + trial + ": After despawn, sound should transition to stopped"
            );
            
            // Verify sound.loop() called once and sound.stop() called once
            verify(mockSound, times(1)).loop();
            verify(mockSound, times(1)).stop(mockSoundId);
            
            manager.dispose();
        }
    }
    
    /**
     * Property: Multiple cycles maintain correct transitions
     * For any sequence of multiple spawn-despawn cycles, each cycle should
     * correctly transition sound state without corruption.
     */
    @Test
    public void multipleCyclesMaintainCorrectTransitions() throws Exception {
        float viewWidth = 800f;
        float viewHeight = 600f;
        
        OrthographicCamera camera = new OrthographicCamera();
        camera.setToOrtho(false, viewWidth, viewHeight);
        Viewport viewport = new FitViewport(viewWidth, viewHeight, camera);
        
        // Run 50 trials (fewer because each does multiple cycles)
        for (int trial = 0; trial < 50; trial++) {
            Sound mockSound = Mockito.mock(Sound.class);
            long mockSoundId = 9000L + trial;
            when(mockSound.loop()).thenReturn(mockSoundId);
            
            com.badlogic.gdx.graphics.Texture mockTexture = Mockito.mock(com.badlogic.gdx.graphics.Texture.class);
            BirdFormationManager manager = new BirdFormationManager(camera, viewport, mockTexture);
            
            // Inject mock sound
            Field soundField = BirdFormationManager.class.getDeclaredField("birdSound");
            soundField.setAccessible(true);
            soundField.set(manager, mockSound);
            
            Field soundIdField = BirdFormationManager.class.getDeclaredField("birdSoundId");
            soundIdField.setAccessible(true);
            
            // Perform 5 spawn-despawn cycles
            for (int cycle = 0; cycle < 5; cycle++) {
                // Before spawn: should be stopped
                long beforeSpawnId = soundIdField.getLong(manager);
                assertEquals(
                    -1L,
                    beforeSpawnId,
                    "Trial " + trial + ", Cycle " + cycle + ": Before spawn, sound should be stopped"
                );
                
                // Spawn
                Field nextSpawnIntervalField = BirdFormationManager.class.getDeclaredField("nextSpawnInterval");
                nextSpawnIntervalField.setAccessible(true);
                float interval = nextSpawnIntervalField.getFloat(manager);
                manager.update(interval + 0.1f, 0, 0);
                
                // After spawn: should be playing
                long afterSpawnId = soundIdField.getLong(manager);
                assertEquals(
                    mockSoundId,
                    afterSpawnId,
                    "Trial " + trial + ", Cycle " + cycle + ": After spawn, sound should be playing"
                );
                
                // Despawn
                float maxUpdateTime = 20f;
                float totalTime = 0f;
                float timeStep = 0.1f;
                
                while (manager.getActiveFormation() != null && totalTime < maxUpdateTime) {
                    manager.update(timeStep, 0, 0);
                    totalTime += timeStep;
                }
                
                // After despawn: should be stopped
                long afterDespawnId = soundIdField.getLong(manager);
                assertEquals(
                    -1L,
                    afterDespawnId,
                    "Trial " + trial + ", Cycle " + cycle + ": After despawn, sound should be stopped"
                );
            }
            
            // Verify sound.loop() and sound.stop() called 5 times each (once per cycle)
            verify(mockSound, times(5)).loop();
            verify(mockSound, times(5)).stop(mockSoundId);
            
            manager.dispose();
        }
    }
    
    /**
     * Property: State transitions are immediate
     * For any spawn or despawn event, the sound state transition should occur
     * immediately without delay.
     */
    @Test
    public void stateTransitionsAreImmediate() throws Exception {
        float viewWidth = 800f;
        float viewHeight = 600f;
        
        OrthographicCamera camera = new OrthographicCamera();
        camera.setToOrtho(false, viewWidth, viewHeight);
        Viewport viewport = new FitViewport(viewWidth, viewHeight, camera);
        
        // Run 100 trials
        for (int trial = 0; trial < 100; trial++) {
            Sound mockSound = Mockito.mock(Sound.class);
            long mockSoundId = 10000L + trial;
            when(mockSound.loop()).thenReturn(mockSoundId);
            
            com.badlogic.gdx.graphics.Texture mockTexture = Mockito.mock(com.badlogic.gdx.graphics.Texture.class);
            BirdFormationManager manager = new BirdFormationManager(camera, viewport, mockTexture);
            
            // Inject mock sound
            Field soundField = BirdFormationManager.class.getDeclaredField("birdSound");
            soundField.setAccessible(true);
            soundField.set(manager, mockSound);
            
            Field soundIdField = BirdFormationManager.class.getDeclaredField("birdSoundId");
            soundIdField.setAccessible(true);
            
            // Trigger spawn
            Field nextSpawnIntervalField = BirdFormationManager.class.getDeclaredField("nextSpawnInterval");
            nextSpawnIntervalField.setAccessible(true);
            float interval = nextSpawnIntervalField.getFloat(manager);
            
            long beforeSpawnId = soundIdField.getLong(manager);
            assertEquals(-1L, beforeSpawnId, "Trial " + trial + ": Before spawn, sound stopped");
            
            // Spawn happens in this update call
            manager.update(interval + 0.1f, 0, 0);
            
            // Immediately after spawn, sound should be playing (no delay)
            long immediatelyAfterSpawnId = soundIdField.getLong(manager);
            assertEquals(
                mockSoundId,
                immediatelyAfterSpawnId,
                "Trial " + trial + ": Immediately after spawn, sound should be playing (no delay)"
            );
            
            // Wait for despawn
            float maxUpdateTime = 20f;
            float totalTime = 0f;
            float timeStep = 0.1f;
            
            BirdFormation lastFormation = manager.getActiveFormation();
            while (manager.getActiveFormation() != null && totalTime < maxUpdateTime) {
                lastFormation = manager.getActiveFormation();
                manager.update(timeStep, 0, 0);
                totalTime += timeStep;
                
                // If formation just despawned, check sound stopped immediately
                if (lastFormation != null && manager.getActiveFormation() == null) {
                    long immediatelyAfterDespawnId = soundIdField.getLong(manager);
                    assertEquals(
                        -1L,
                        immediatelyAfterDespawnId,
                        "Trial " + trial + ": Immediately after despawn, sound should be stopped (no delay)"
                    );
                }
            }
            
            manager.dispose();
        }
    }
    
    /**
     * Property: No state corruption across cycles
     * For any sequence of cycles, the sound state should never become corrupted
     * (e.g., soundId set to invalid value other than -1 or valid ID).
     */
    @Test
    public void noStateCorruptionAcrossCycles() throws Exception {
        float viewWidth = 800f;
        float viewHeight = 600f;
        
        OrthographicCamera camera = new OrthographicCamera();
        camera.setToOrtho(false, viewWidth, viewHeight);
        Viewport viewport = new FitViewport(viewWidth, viewHeight, camera);
        
        // Run 50 trials
        for (int trial = 0; trial < 50; trial++) {
            Sound mockSound = Mockito.mock(Sound.class);
            long mockSoundId = 11000L + trial;
            when(mockSound.loop()).thenReturn(mockSoundId);
            
            com.badlogic.gdx.graphics.Texture mockTexture = Mockito.mock(com.badlogic.gdx.graphics.Texture.class);
            BirdFormationManager manager = new BirdFormationManager(camera, viewport, mockTexture);
            
            // Inject mock sound
            Field soundField = BirdFormationManager.class.getDeclaredField("birdSound");
            soundField.setAccessible(true);
            soundField.set(manager, mockSound);
            
            Field soundIdField = BirdFormationManager.class.getDeclaredField("birdSoundId");
            soundIdField.setAccessible(true);
            
            // Perform 3 cycles and check for corruption after each operation
            for (int cycle = 0; cycle < 3; cycle++) {
                // Spawn
                Field nextSpawnIntervalField = BirdFormationManager.class.getDeclaredField("nextSpawnInterval");
                nextSpawnIntervalField.setAccessible(true);
                float interval = nextSpawnIntervalField.getFloat(manager);
                manager.update(interval + 0.1f, 0, 0);
                
                long afterSpawnId = soundIdField.getLong(manager);
                // Sound ID should be either -1 (stopped) or mockSoundId (playing), nothing else
                assertTrue(
                    afterSpawnId == -1L || afterSpawnId == mockSoundId,
                    "Trial " + trial + ", Cycle " + cycle + ": After spawn, soundId should be valid (-1 or " + mockSoundId + "), got: " + afterSpawnId
                );
                
                // Despawn
                float maxUpdateTime = 20f;
                float totalTime = 0f;
                float timeStep = 0.1f;
                
                while (manager.getActiveFormation() != null && totalTime < maxUpdateTime) {
                    manager.update(timeStep, 0, 0);
                    
                    long duringUpdateId = soundIdField.getLong(manager);
                    assertTrue(
                        duringUpdateId == -1L || duringUpdateId == mockSoundId,
                        "Trial " + trial + ", Cycle " + cycle + ": During update, soundId should be valid, got: " + duringUpdateId
                    );
                    
                    totalTime += timeStep;
                }
                
                long afterDespawnId = soundIdField.getLong(manager);
                assertTrue(
                    afterDespawnId == -1L || afterDespawnId == mockSoundId,
                    "Trial " + trial + ", Cycle " + cycle + ": After despawn, soundId should be valid, got: " + afterDespawnId
                );
            }
            
            manager.dispose();
        }
    }
}
