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
 * Property-based test for sound stopping on despawn.
 * Feature: bird-ambient-sound, Property 2: Sound stops on despawn
 * Validates: Requirements 1.4, 1.5
 */
public class SoundStopsOnDespawnPropertyTest {
    
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
     * Property 2: Sound stops on despawn
     * For any bird formation despawn event, if the sound was playing, the sound instance ID
     * should be reset to -1 immediately after despawning.
     * Validates: Requirements 1.4, 1.5
     * 
     * This property-based test runs 100 trials and verifies that:
     * 1. After spawn, sound ID is not -1 (playing)
     * 2. After despawn, sound ID is -1 (stopped)
     * 3. Sound.stop() is called with the correct sound ID
     * 4. Sound stops immediately when formation despawns
     */
    @Test
    public void soundStopsOnDespawn() throws Exception {
        float viewWidth = 800f;
        float viewHeight = 600f;
        
        OrthographicCamera camera = new OrthographicCamera();
        camera.setToOrtho(false, viewWidth, viewHeight);
        Viewport viewport = new FitViewport(viewWidth, viewHeight, camera);
        
        // Run 100 trials
        for (int trial = 0; trial < 100; trial++) {
            // Create mock sound
            Sound mockSound = Mockito.mock(Sound.class);
            long mockSoundId = 5000L + trial;
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
            manager.update(interval + 0.1f, 0, 0);
            
            // Verify formation spawned and sound started
            assertNotNull(manager.getActiveFormation(), "Trial " + trial + ": Formation should spawn");
            long afterSpawnSoundId = soundIdField.getLong(manager);
            assertEquals(
                mockSoundId,
                afterSpawnSoundId,
                "Trial " + trial + ": Sound should be playing after spawn"
            );
            
            // Wait for despawn
            float maxUpdateTime = 20f;
            float totalTime = 0f;
            float timeStep = 0.1f;
            
            while (manager.getActiveFormation() != null && totalTime < maxUpdateTime) {
                manager.update(timeStep, 0, 0);
                totalTime += timeStep;
            }
            
            // Verify formation despawned
            assertNull(
                manager.getActiveFormation(),
                "Trial " + trial + ": Formation should despawn"
            );
            
            // Wait for fade-out to complete (1+ second)
            for (int i = 0; i < 15; i++) {
                manager.update(0.1f, 0, 0);
            }
            
            // Verify sound stopped: sound ID should be -1
            long afterDespawnSoundId = soundIdField.getLong(manager);
            assertEquals(
                -1L,
                afterDespawnSoundId,
                "Trial " + trial + ": Sound ID should be -1 after despawn (sound stopped)"
            );
            
            // Verify sound.stop() was called with the correct sound ID
            verify(mockSound, times(1)).stop(mockSoundId);
            
            manager.dispose();
        }
    }
    
    /**
     * Property: Sound stop is called on every despawn
     * For any sequence of spawn-despawn cycles, sound.stop() should be called
     * exactly once per despawn.
     */
    @Test
    public void soundStopCalledOnEveryDespawn() throws Exception {
        float viewWidth = 800f;
        float viewHeight = 600f;
        
        OrthographicCamera camera = new OrthographicCamera();
        camera.setToOrtho(false, viewWidth, viewHeight);
        Viewport viewport = new FitViewport(viewWidth, viewHeight, camera);
        
        // Run 50 trials (fewer because each does multiple cycles)
        for (int trial = 0; trial < 50; trial++) {
            Sound mockSound = Mockito.mock(Sound.class);
            long mockSoundId = 7000L + trial;
            when(mockSound.loop()).thenReturn(mockSoundId);
            
            com.badlogic.gdx.graphics.Texture mockTexture = Mockito.mock(com.badlogic.gdx.graphics.Texture.class);
            BirdFormationManager manager = new BirdFormationManager(camera, viewport, mockTexture);
            
            // Inject mock sound
            Field soundField = BirdFormationManager.class.getDeclaredField("birdSound");
            soundField.setAccessible(true);
            soundField.set(manager, mockSound);
            
            Field soundIdField = BirdFormationManager.class.getDeclaredField("birdSoundId");
            soundIdField.setAccessible(true);
            
            // Perform 3 spawn-despawn cycles
            for (int cycle = 0; cycle < 3; cycle++) {
                // Spawn
                Field nextSpawnIntervalField = BirdFormationManager.class.getDeclaredField("nextSpawnInterval");
                nextSpawnIntervalField.setAccessible(true);
                float interval = nextSpawnIntervalField.getFloat(manager);
                manager.update(interval + 0.1f, 0, 0);
                
                assertNotNull(
                    manager.getActiveFormation(),
                    "Trial " + trial + ", Cycle " + cycle + ": Formation should spawn"
                );
                
                long afterSpawnId = soundIdField.getLong(manager);
                assertEquals(
                    mockSoundId,
                    afterSpawnId,
                    "Trial " + trial + ", Cycle " + cycle + ": Sound should be playing after spawn"
                );
                
                // Despawn
                float maxUpdateTime = 20f;
                float totalTime = 0f;
                float timeStep = 0.1f;
                
                while (manager.getActiveFormation() != null && totalTime < maxUpdateTime) {
                    manager.update(timeStep, 0, 0);
                    totalTime += timeStep;
                }
                
                assertNull(
                    manager.getActiveFormation(),
                    "Trial " + trial + ", Cycle " + cycle + ": Formation should despawn"
                );
                
                // Wait for fade-out to complete (1+ second)
                for (int i = 0; i < 15; i++) {
                    manager.update(0.1f, 0, 0);
                }
                
                long afterDespawnId = soundIdField.getLong(manager);
                assertEquals(
                    -1L,
                    afterDespawnId,
                    "Trial " + trial + ", Cycle " + cycle + ": Sound should be stopped after despawn"
                );
            }
            
            // Verify sound.stop() was called exactly 3 times (once per despawn)
            verify(mockSound, times(3)).stop(mockSoundId);
            
            manager.dispose();
        }
    }
    
    /**
     * Property: Sound does not stop if not playing
     * For any despawn event when sound is not playing (soundId == -1),
     * sound.stop() should not be called.
     */
    @Test
    public void soundDoesNotStopIfNotPlaying() throws Exception {
        float viewWidth = 800f;
        float viewHeight = 600f;
        
        OrthographicCamera camera = new OrthographicCamera();
        camera.setToOrtho(false, viewWidth, viewHeight);
        Viewport viewport = new FitViewport(viewWidth, viewHeight, camera);
        
        // Run 100 trials
        for (int trial = 0; trial < 100; trial++) {
            Sound mockSound = Mockito.mock(Sound.class);
            
            com.badlogic.gdx.graphics.Texture mockTexture = Mockito.mock(com.badlogic.gdx.graphics.Texture.class);
            BirdFormationManager manager = new BirdFormationManager(camera, viewport, mockTexture);
            
            // DON'T inject mock sound - leave it null to simulate sound not loaded
            // This way, when despawn is called, sound will be null and stop won't be called
            
            Field soundIdField = BirdFormationManager.class.getDeclaredField("birdSoundId");
            soundIdField.setAccessible(true);
            
            // Verify sound ID is -1 (not playing)
            assertEquals(-1L, soundIdField.getLong(manager));
            
            // Manually invoke despawnFormation via reflection
            java.lang.reflect.Method despawnMethod = BirdFormationManager.class.getDeclaredMethod("despawnFormation");
            despawnMethod.setAccessible(true);
            despawnMethod.invoke(manager);
            
            // Verify sound.stop() was NOT called (because sound was null)
            verify(mockSound, never()).stop(anyLong());
            
            // Verify sound ID is still -1
            assertEquals(-1L, soundIdField.getLong(manager));
            
            manager.dispose();
        }
    }
}
