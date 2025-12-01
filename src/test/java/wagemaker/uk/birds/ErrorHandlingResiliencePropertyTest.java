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
 * Property-based test for error handling resilience.
 * Feature: bird-ambient-sound, Property 8: Error handling resilience
 * Validates: Requirements 4.1, 4.4
 */
public class ErrorHandlingResiliencePropertyTest {
    
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
     * Property 8: Error handling resilience
     * For any sound loading failure or playback error, the system should log the error
     * and continue execution without crashing.
     * Validates: Requirements 4.1, 4.4
     * 
     * This property-based test runs 100 trials and verifies that:
     * 1. When sound.loop() throws exception, system continues without crashing
     * 2. When sound.stop() throws exception, system continues without crashing
     * 3. When sound.dispose() throws exception, system continues without crashing
     * 4. Sound ID is properly managed even when errors occur
     */
    @Test
    public void errorHandlingResilienceForSoundLoop() throws Exception {
        float viewWidth = 800f;
        float viewHeight = 600f;
        
        OrthographicCamera camera = new OrthographicCamera();
        camera.setToOrtho(false, viewWidth, viewHeight);
        Viewport viewport = new FitViewport(viewWidth, viewHeight, camera);
        
        // Run 100 trials
        for (int trial = 0; trial < 100; trial++) {
            Sound mockSound = Mockito.mock(Sound.class);
            // Make sound.loop() throw exception
            when(mockSound.loop()).thenThrow(new RuntimeException("Sound playback error"));
            
            com.badlogic.gdx.graphics.Texture mockTexture = Mockito.mock(com.badlogic.gdx.graphics.Texture.class);
            BirdFormationManager manager = new BirdFormationManager(camera, viewport, mockTexture);
            
            // Inject mock sound
            Field soundField = BirdFormationManager.class.getDeclaredField("birdSound");
            soundField.setAccessible(true);
            soundField.set(manager, mockSound);
            
            Field soundIdField = BirdFormationManager.class.getDeclaredField("birdSoundId");
            soundIdField.setAccessible(true);
            
            // Trigger spawn - should not crash despite sound.loop() throwing exception
            assertDoesNotThrow(
                () -> {
                    Field nextSpawnIntervalField = BirdFormationManager.class.getDeclaredField("nextSpawnInterval");
                    nextSpawnIntervalField.setAccessible(true);
                    float interval = nextSpawnIntervalField.getFloat(manager);
                    manager.update(interval + 0.1f, 0, 0);
                },
                "Trial " + trial + ": Spawn should not crash when sound.loop() throws exception"
            );
            
            // Verify formation still spawned despite sound error
            assertNotNull(
                manager.getActiveFormation(),
                "Trial " + trial + ": Formation should spawn even when sound.loop() fails"
            );
            
            // Verify sound ID remains -1 (sound didn't start due to error)
            long soundId = soundIdField.getLong(manager);
            assertEquals(
                -1L,
                soundId,
                "Trial " + trial + ": Sound ID should remain -1 when sound.loop() fails"
            );
            
            manager.dispose();
        }
    }
    
    /**
     * Property: Error handling resilience for sound.stop()
     * For any error during sound.stop(), the system should continue without crashing
     * and properly reset sound ID.
     */
    @Test
    public void errorHandlingResilienceForSoundStop() throws Exception {
        float viewWidth = 800f;
        float viewHeight = 600f;
        
        OrthographicCamera camera = new OrthographicCamera();
        camera.setToOrtho(false, viewWidth, viewHeight);
        Viewport viewport = new FitViewport(viewWidth, viewHeight, camera);
        
        // Run 100 trials
        for (int trial = 0; trial < 100; trial++) {
            Sound mockSound = Mockito.mock(Sound.class);
            long mockSoundId = 17000L + trial;
            when(mockSound.loop()).thenReturn(mockSoundId);
            // Make sound.stop() throw exception
            doThrow(new RuntimeException("Sound stop error")).when(mockSound).stop(mockSoundId);
            
            com.badlogic.gdx.graphics.Texture mockTexture = Mockito.mock(com.badlogic.gdx.graphics.Texture.class);
            BirdFormationManager manager = new BirdFormationManager(camera, viewport, mockTexture);
            
            // Inject mock sound
            Field soundField = BirdFormationManager.class.getDeclaredField("birdSound");
            soundField.setAccessible(true);
            soundField.set(manager, mockSound);
            
            Field soundIdField = BirdFormationManager.class.getDeclaredField("birdSoundId");
            soundIdField.setAccessible(true);
            
            // Spawn formation
            Field nextSpawnIntervalField = BirdFormationManager.class.getDeclaredField("nextSpawnInterval");
            nextSpawnIntervalField.setAccessible(true);
            float interval = nextSpawnIntervalField.getFloat(manager);
            manager.update(interval + 0.1f, 0, 0);
            
            // Verify sound started
            assertEquals(mockSoundId, soundIdField.getLong(manager));
            
            // Wait for despawn - should not crash despite sound.stop() throwing exception
            assertDoesNotThrow(
                () -> {
                    float maxUpdateTime = 20f;
                    float totalTime = 0f;
                    float timeStep = 0.1f;
                    
                    while (manager.getActiveFormation() != null && totalTime < maxUpdateTime) {
                        manager.update(timeStep, 0, 0);
                        totalTime += timeStep;
                    }
                    
                    // Wait for fade-out to complete (1+ second)
                    for (int i = 0; i < 15; i++) {
                        manager.update(0.1f, 0, 0);
                    }
                },
                "Trial " + trial + ": Despawn should not crash when sound.stop() throws exception"
            );
            
            // Verify formation despawned despite sound.stop() error
            assertNull(
                manager.getActiveFormation(),
                "Trial " + trial + ": Formation should despawn even when sound.stop() fails"
            );
            
            // Verify sound ID reset to -1 despite error (reset happens in finally block)
            long afterDespawnId = soundIdField.getLong(manager);
            assertEquals(
                -1L,
                afterDespawnId,
                "Trial " + trial + ": Sound ID should be reset to -1 even when sound.stop() fails"
            );;
            
            manager.dispose();
        }
    }
    
    /**
     * Property: Error handling resilience for sound.dispose()
     * For any error during sound.dispose(), the system should continue without crashing
     * and properly set sound to null.
     */
    @Test
    public void errorHandlingResilienceForSoundDispose() throws Exception {
        float viewWidth = 800f;
        float viewHeight = 600f;
        
        OrthographicCamera camera = new OrthographicCamera();
        camera.setToOrtho(false, viewWidth, viewHeight);
        Viewport viewport = new FitViewport(viewWidth, viewHeight, camera);
        
        // Run 100 trials
        for (int trial = 0; trial < 100; trial++) {
            Sound mockSound = Mockito.mock(Sound.class);
            // Make sound.dispose() throw exception
            doThrow(new RuntimeException("Sound dispose error")).when(mockSound).dispose();
            
            com.badlogic.gdx.graphics.Texture mockTexture = Mockito.mock(com.badlogic.gdx.graphics.Texture.class);
            BirdFormationManager manager = new BirdFormationManager(camera, viewport, mockTexture);
            
            // Inject mock sound
            Field soundField = BirdFormationManager.class.getDeclaredField("birdSound");
            soundField.setAccessible(true);
            soundField.set(manager, mockSound);
            
            // Dispose - should not crash despite sound.dispose() throwing exception
            assertDoesNotThrow(
                () -> manager.dispose(),
                "Trial " + trial + ": Dispose should not crash when sound.dispose() throws exception"
            );
            
            // Verify sound set to null despite dispose error
            Sound afterDisposeSound = (Sound) soundField.get(manager);
            assertNull(
                afterDisposeSound,
                "Trial " + trial + ": Sound should be set to null even when sound.dispose() fails"
            );
        }
    }
    
    /**
     * Property: System continues functioning after sound errors
     * For any sound error, the system should continue functioning normally
     * for subsequent operations.
     */
    @Test
    public void systemContinuesFunctioningAfterSoundErrors() throws Exception {
        float viewWidth = 800f;
        float viewHeight = 600f;
        
        OrthographicCamera camera = new OrthographicCamera();
        camera.setToOrtho(false, viewWidth, viewHeight);
        Viewport viewport = new FitViewport(viewWidth, viewHeight, camera);
        
        // Run 50 trials
        for (int trial = 0; trial < 50; trial++) {
            Sound mockSound = Mockito.mock(Sound.class);
            // First call throws exception, subsequent calls work
            when(mockSound.loop())
                .thenThrow(new RuntimeException("First call fails"))
                .thenReturn(18000L + trial);
            
            com.badlogic.gdx.graphics.Texture mockTexture = Mockito.mock(com.badlogic.gdx.graphics.Texture.class);
            BirdFormationManager manager = new BirdFormationManager(camera, viewport, mockTexture);
            
            // Inject mock sound
            Field soundField = BirdFormationManager.class.getDeclaredField("birdSound");
            soundField.setAccessible(true);
            soundField.set(manager, mockSound);
            
            Field soundIdField = BirdFormationManager.class.getDeclaredField("birdSoundId");
            soundIdField.setAccessible(true);
            
            // First spawn - sound.loop() will fail
            Field nextSpawnIntervalField = BirdFormationManager.class.getDeclaredField("nextSpawnInterval");
            nextSpawnIntervalField.setAccessible(true);
            float interval = nextSpawnIntervalField.getFloat(manager);
            manager.update(interval + 0.1f, 0, 0);
            
            // Verify formation spawned despite sound error
            assertNotNull(
                manager.getActiveFormation(),
                "Trial " + trial + ": First formation should spawn despite sound error"
            );
            
            // Wait for despawn
            float maxUpdateTime = 20f;
            float totalTime = 0f;
            float timeStep = 0.1f;
            
            while (manager.getActiveFormation() != null && totalTime < maxUpdateTime) {
                manager.update(timeStep, 0, 0);
                totalTime += timeStep;
            }
            
            // Second spawn - sound.loop() should work now
            interval = nextSpawnIntervalField.getFloat(manager);
            manager.update(interval + 0.1f, 0, 0);
            
            // Verify second formation spawned
            assertNotNull(
                manager.getActiveFormation(),
                "Trial " + trial + ": Second formation should spawn after previous sound error"
            );
            
            // Verify sound started on second spawn
            long soundId = soundIdField.getLong(manager);
            assertEquals(
                18000L + trial,
                soundId,
                "Trial " + trial + ": Sound should start on second spawn after previous error"
            );
            
            manager.dispose();
        }
    }
    
    /**
     * Property: Multiple consecutive errors don't crash system
     * For any sequence of sound errors, the system should remain stable
     * and continue functioning.
     */
    @Test
    public void multipleConsecutiveErrorsDontCrashSystem() throws Exception {
        float viewWidth = 800f;
        float viewHeight = 600f;
        
        OrthographicCamera camera = new OrthographicCamera();
        camera.setToOrtho(false, viewWidth, viewHeight);
        Viewport viewport = new FitViewport(viewWidth, viewHeight, camera);
        
        // Run 100 trials
        for (int trial = 0; trial < 100; trial++) {
            Sound mockSound = Mockito.mock(Sound.class);
            // All calls throw exceptions
            when(mockSound.loop()).thenThrow(new RuntimeException("Loop error"));
            doThrow(new RuntimeException("Stop error")).when(mockSound).stop(anyLong());
            doThrow(new RuntimeException("Dispose error")).when(mockSound).dispose();
            
            com.badlogic.gdx.graphics.Texture mockTexture = Mockito.mock(com.badlogic.gdx.graphics.Texture.class);
            BirdFormationManager manager = new BirdFormationManager(camera, viewport, mockTexture);
            
            // Inject mock sound
            Field soundField = BirdFormationManager.class.getDeclaredField("birdSound");
            soundField.setAccessible(true);
            soundField.set(manager, mockSound);
            
            // Perform multiple operations - none should crash
            assertDoesNotThrow(
                () -> {
                    // Spawn
                    Field nextSpawnIntervalField = BirdFormationManager.class.getDeclaredField("nextSpawnInterval");
                    nextSpawnIntervalField.setAccessible(true);
                    float interval = nextSpawnIntervalField.getFloat(manager);
                    manager.update(interval + 0.1f, 0, 0);
                    
                    // Wait for despawn
                    float maxUpdateTime = 20f;
                    float totalTime = 0f;
                    float timeStep = 0.1f;
                    
                    while (manager.getActiveFormation() != null && totalTime < maxUpdateTime) {
                        manager.update(timeStep, 0, 0);
                        totalTime += timeStep;
                    }
                    
                    // Dispose
                    manager.dispose();
                },
                "Trial " + trial + ": System should not crash despite multiple consecutive sound errors"
            );
        }
    }
}
