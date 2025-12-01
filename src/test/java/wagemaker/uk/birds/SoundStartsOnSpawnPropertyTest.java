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
 * Property-based test for sound starting on spawn.
 * Feature: bird-ambient-sound, Property 1: Sound starts on spawn
 * Validates: Requirements 1.1, 1.2
 */
public class SoundStartsOnSpawnPropertyTest {
    
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
     * Property 1: Sound starts on spawn
     * For any bird formation spawn event, if the sound is loaded, the sound instance ID
     * should be set to a valid value (not -1) immediately after spawning.
     * Validates: Requirements 1.1, 1.2
     * 
     * This property-based test runs 100 trials and verifies that:
     * 1. Before spawn, sound ID is -1 (not playing)
     * 2. After spawn with loaded sound, sound ID is not -1 (playing)
     * 3. Sound.loop() is called exactly once per spawn
     * 4. Sound starts immediately when formation spawns
     */
    @Test
    public void soundStartsOnSpawn() throws Exception {
        float viewWidth = 800f;
        float viewHeight = 600f;
        
        OrthographicCamera camera = new OrthographicCamera();
        camera.setToOrtho(false, viewWidth, viewHeight);
        Viewport viewport = new FitViewport(viewWidth, viewHeight, camera);
        
        // Run 100 trials
        for (int trial = 0; trial < 100; trial++) {
            // Create mock sound and texture
            Sound mockSound = Mockito.mock(Sound.class);
            long mockSoundId = 12345L + trial; // Unique ID for each trial
            when(mockSound.loop()).thenReturn(mockSoundId);
            
            com.badlogic.gdx.graphics.Texture mockTexture = Mockito.mock(com.badlogic.gdx.graphics.Texture.class);
            
            // Create manager with test constructor (includes texture)
            BirdFormationManager manager = new BirdFormationManager(camera, viewport, mockTexture);
            
            // Use reflection to inject mock sound
            Field soundField = BirdFormationManager.class.getDeclaredField("birdSound");
            soundField.setAccessible(true);
            soundField.set(manager, mockSound);
            
            // Get sound ID field for verification
            Field soundIdField = BirdFormationManager.class.getDeclaredField("birdSoundId");
            soundIdField.setAccessible(true);
            
            // Verify initial state: sound ID should be -1 (not playing)
            long initialSoundId = soundIdField.getLong(manager);
            assertEquals(
                -1L,
                initialSoundId,
                "Trial " + trial + ": Sound ID should be -1 before spawn"
            );
            
            // Trigger spawn by advancing time past spawn interval
            Field nextSpawnIntervalField = BirdFormationManager.class.getDeclaredField("nextSpawnInterval");
            nextSpawnIntervalField.setAccessible(true);
            float interval = nextSpawnIntervalField.getFloat(manager);
            
            manager.update(interval + 0.1f, 0, 0);
            
            // Verify formation was spawned
            BirdFormation formation = manager.getActiveFormation();
            assertNotNull(
                formation,
                "Trial " + trial + ": Formation should be spawned"
            );
            
            // Verify sound started: sound ID should not be -1
            long afterSpawnSoundId = soundIdField.getLong(manager);
            assertNotEquals(
                -1L,
                afterSpawnSoundId,
                "Trial " + trial + ": Sound ID should not be -1 after spawn (sound should be playing)"
            );
            
            // Verify sound ID matches the mock return value
            assertEquals(
                mockSoundId,
                afterSpawnSoundId,
                "Trial " + trial + ": Sound ID should match the value returned by sound.loop()"
            );
            
            // Verify sound.loop() was called exactly once
            verify(mockSound, times(1)).loop();
            
            // Clean up
            manager.dispose();
        }
    }
    
    /**
     * Property: Sound loop is called on spawn
     * For any spawn event with loaded sound, sound.loop() should be called exactly once.
     */
    @Test
    public void soundLoopCalledOnSpawn() throws Exception {
        float viewWidth = 800f;
        float viewHeight = 600f;
        
        OrthographicCamera camera = new OrthographicCamera();
        camera.setToOrtho(false, viewWidth, viewHeight);
        Viewport viewport = new FitViewport(viewWidth, viewHeight, camera);
        
        // Run 100 trials
        for (int trial = 0; trial < 100; trial++) {
            Sound mockSound = Mockito.mock(Sound.class);
            when(mockSound.loop()).thenReturn(100L + trial);
            
            com.badlogic.gdx.graphics.Texture mockTexture = Mockito.mock(com.badlogic.gdx.graphics.Texture.class);
            BirdFormationManager manager = new BirdFormationManager(camera, viewport, mockTexture);
            
            // Inject mock sound
            Field soundField = BirdFormationManager.class.getDeclaredField("birdSound");
            soundField.setAccessible(true);
            soundField.set(manager, mockSound);
            
            Field soundIdField = BirdFormationManager.class.getDeclaredField("birdSoundId");
            soundIdField.setAccessible(true);
            
            // Verify sound not playing before spawn
            long beforeSpawnId = soundIdField.getLong(manager);
            assertEquals(
                -1L,
                beforeSpawnId,
                "Trial " + trial + ": Sound should not be playing before spawn"
            );
            
            // Trigger spawn
            Field nextSpawnIntervalField = BirdFormationManager.class.getDeclaredField("nextSpawnInterval");
            nextSpawnIntervalField.setAccessible(true);
            float interval = nextSpawnIntervalField.getFloat(manager);
            manager.update(interval + 0.1f, 0, 0);
            
            // Verify sound started
            long afterSpawnId = soundIdField.getLong(manager);
            assertNotEquals(
                -1L,
                afterSpawnId,
                "Trial " + trial + ": Sound should be playing after spawn"
            );
            
            // Verify sound.loop() was called exactly once
            verify(mockSound, times(1)).loop();
            
            manager.dispose();
        }
    }
    
    /**
     * Property: Sound does not start if already playing
     * For any spawn event when sound is already playing (soundId != -1),
     * sound.loop() should not be called again.
     */
    @Test
    public void soundDoesNotStartIfAlreadyPlaying() throws Exception {
        float viewWidth = 800f;
        float viewHeight = 600f;
        
        OrthographicCamera camera = new OrthographicCamera();
        camera.setToOrtho(false, viewWidth, viewHeight);
        Viewport viewport = new FitViewport(viewWidth, viewHeight, camera);
        
        // Run 100 trials
        for (int trial = 0; trial < 100; trial++) {
            Sound mockSound = Mockito.mock(Sound.class);
            when(mockSound.loop()).thenReturn(999L);
            
            com.badlogic.gdx.graphics.Texture mockTexture = Mockito.mock(com.badlogic.gdx.graphics.Texture.class);
            BirdFormationManager manager = new BirdFormationManager(camera, viewport, mockTexture);
            
            // Inject mock sound
            Field soundField = BirdFormationManager.class.getDeclaredField("birdSound");
            soundField.setAccessible(true);
            soundField.set(manager, mockSound);
            
            // Manually set sound ID to simulate sound already playing
            Field soundIdField = BirdFormationManager.class.getDeclaredField("birdSoundId");
            soundIdField.setAccessible(true);
            soundIdField.setLong(manager, 888L); // Simulate sound already playing
            
            // Trigger spawn
            Field nextSpawnIntervalField = BirdFormationManager.class.getDeclaredField("nextSpawnInterval");
            nextSpawnIntervalField.setAccessible(true);
            float interval = nextSpawnIntervalField.getFloat(manager);
            manager.update(interval + 0.1f, 0, 0);
            
            // Verify formation spawned
            assertNotNull(
                manager.getActiveFormation(),
                "Trial " + trial + ": Formation should spawn"
            );
            
            // Verify sound.loop() was NOT called (because sound was already playing)
            verify(mockSound, never()).loop();
            
            // Verify sound ID unchanged (still the original value)
            long soundId = soundIdField.getLong(manager);
            assertEquals(
                888L,
                soundId,
                "Trial " + trial + ": Sound ID should remain unchanged when sound already playing"
            );
            
            manager.dispose();
        }
    }
}
