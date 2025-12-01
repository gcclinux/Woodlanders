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
 * Property-based test for disposal cleanup.
 * Feature: bird-ambient-sound, Property 6: Disposal cleanup
 * Validates: Requirements 3.4, 5.1, 5.2
 */
public class DisposalCleanupPropertyTest {
    
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
     * Property 6: Disposal cleanup
     * For any call to dispose(), if the sound was playing, it should be stopped,
     * and the Sound object should be disposed.
     * Validates: Requirements 3.4, 5.1, 5.2
     * 
     * This property-based test runs 100 trials and verifies that:
     * 1. If sound is playing, dispose() stops it
     * 2. dispose() calls sound.dispose()
     * 3. Sound ID is reset to -1 after disposal
     * 4. Sound object is set to null after disposal
     */
    @Test
    public void disposalCleanupWhenSoundPlaying() throws Exception {
        float viewWidth = 800f;
        float viewHeight = 600f;
        
        OrthographicCamera camera = new OrthographicCamera();
        camera.setToOrtho(false, viewWidth, viewHeight);
        Viewport viewport = new FitViewport(viewWidth, viewHeight, camera);
        
        // Run 100 trials
        for (int trial = 0; trial < 100; trial++) {
            Sound mockSound = Mockito.mock(Sound.class);
            long mockSoundId = 12000L + trial;
            when(mockSound.loop()).thenReturn(mockSoundId);
            
            com.badlogic.gdx.graphics.Texture mockTexture = Mockito.mock(com.badlogic.gdx.graphics.Texture.class);
            BirdFormationManager manager = new BirdFormationManager(camera, viewport, mockTexture);
            
            // Inject mock sound
            Field soundField = BirdFormationManager.class.getDeclaredField("birdSound");
            soundField.setAccessible(true);
            soundField.set(manager, mockSound);
            
            Field soundIdField = BirdFormationManager.class.getDeclaredField("birdSoundId");
            soundIdField.setAccessible(true);
            
            // Spawn formation to start sound
            Field nextSpawnIntervalField = BirdFormationManager.class.getDeclaredField("nextSpawnInterval");
            nextSpawnIntervalField.setAccessible(true);
            float interval = nextSpawnIntervalField.getFloat(manager);
            manager.update(interval + 0.1f, 0, 0);
            
            // Verify sound is playing
            long beforeDisposeId = soundIdField.getLong(manager);
            assertEquals(
                mockSoundId,
                beforeDisposeId,
                "Trial " + trial + ": Sound should be playing before dispose"
            );
            
            // Dispose manager
            manager.dispose();
            
            // Verify sound was stopped
            verify(mockSound, times(1)).stop(mockSoundId);
            
            // Verify sound was disposed
            verify(mockSound, times(1)).dispose();
            
            // Verify sound ID reset to -1
            long afterDisposeId = soundIdField.getLong(manager);
            assertEquals(
                -1L,
                afterDisposeId,
                "Trial " + trial + ": Sound ID should be -1 after dispose"
            );
            
            // Verify sound object set to null
            Sound afterDisposeSound = (Sound) soundField.get(manager);
            assertNull(
                afterDisposeSound,
                "Trial " + trial + ": Sound object should be null after dispose"
            );
        }
    }
    
    /**
     * Property: Disposal cleanup when sound not playing
     * For any call to dispose() when sound is not playing, the Sound object
     * should still be disposed properly.
     */
    @Test
    public void disposalCleanupWhenSoundNotPlaying() throws Exception {
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
            
            // Inject mock sound but don't start it
            Field soundField = BirdFormationManager.class.getDeclaredField("birdSound");
            soundField.setAccessible(true);
            soundField.set(manager, mockSound);
            
            Field soundIdField = BirdFormationManager.class.getDeclaredField("birdSoundId");
            soundIdField.setAccessible(true);
            
            // Verify sound is not playing
            long beforeDisposeId = soundIdField.getLong(manager);
            assertEquals(
                -1L,
                beforeDisposeId,
                "Trial " + trial + ": Sound should not be playing before dispose"
            );
            
            // Dispose manager
            manager.dispose();
            
            // Verify sound.stop() was NOT called (sound wasn't playing)
            verify(mockSound, never()).stop(anyLong());
            
            // Verify sound was still disposed
            verify(mockSound, times(1)).dispose();
            
            // Verify sound ID still -1
            long afterDisposeId = soundIdField.getLong(manager);
            assertEquals(
                -1L,
                afterDisposeId,
                "Trial " + trial + ": Sound ID should remain -1 after dispose"
            );
            
            // Verify sound object set to null
            Sound afterDisposeSound = (Sound) soundField.get(manager);
            assertNull(
                afterDisposeSound,
                "Trial " + trial + ": Sound object should be null after dispose"
            );
        }
    }
    
    /**
     * Property: Disposal cleanup after complete lifecycle
     * For any complete spawn-despawn cycle followed by dispose, all resources
     * should be properly cleaned up.
     */
    @Test
    public void disposalCleanupAfterCompleteLifecycle() throws Exception {
        float viewWidth = 800f;
        float viewHeight = 600f;
        
        OrthographicCamera camera = new OrthographicCamera();
        camera.setToOrtho(false, viewWidth, viewHeight);
        Viewport viewport = new FitViewport(viewWidth, viewHeight, camera);
        
        // Run 100 trials
        for (int trial = 0; trial < 100; trial++) {
            Sound mockSound = Mockito.mock(Sound.class);
            long mockSoundId = 13000L + trial;
            when(mockSound.loop()).thenReturn(mockSoundId);
            
            com.badlogic.gdx.graphics.Texture mockTexture = Mockito.mock(com.badlogic.gdx.graphics.Texture.class);
            BirdFormationManager manager = new BirdFormationManager(camera, viewport, mockTexture);
            
            // Inject mock sound
            Field soundField = BirdFormationManager.class.getDeclaredField("birdSound");
            soundField.setAccessible(true);
            soundField.set(manager, mockSound);
            
            Field soundIdField = BirdFormationManager.class.getDeclaredField("birdSoundId");
            soundIdField.setAccessible(true);
            
            // Complete spawn-despawn cycle
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
            
            // Verify sound stopped after despawn
            long afterDespawnId = soundIdField.getLong(manager);
            assertEquals(
                -1L,
                afterDespawnId,
                "Trial " + trial + ": Sound should be stopped after despawn"
            );
            
            // Now dispose
            manager.dispose();
            
            // Verify sound.stop() was called once (during despawn, not during dispose)
            verify(mockSound, times(1)).stop(mockSoundId);
            
            // Verify sound was disposed
            verify(mockSound, times(1)).dispose();
            
            // Verify sound object set to null
            Sound afterDisposeSound = (Sound) soundField.get(manager);
            assertNull(
                afterDisposeSound,
                "Trial " + trial + ": Sound object should be null after dispose"
            );
        }
    }
    
    /**
     * Property: Disposal with null sound is safe
     * For any manager with null sound, calling dispose() should not throw exceptions.
     */
    @Test
    public void disposalWithNullSoundIsSafe() throws Exception {
        float viewWidth = 800f;
        float viewHeight = 600f;
        
        OrthographicCamera camera = new OrthographicCamera();
        camera.setToOrtho(false, viewWidth, viewHeight);
        Viewport viewport = new FitViewport(viewWidth, viewHeight, camera);
        
        // Run 100 trials
        for (int trial = 0; trial < 100; trial++) {
            com.badlogic.gdx.graphics.Texture mockTexture = Mockito.mock(com.badlogic.gdx.graphics.Texture.class);
            BirdFormationManager manager = new BirdFormationManager(camera, viewport, mockTexture);
            
            // Don't inject sound - leave it null
            
            // Dispose should not throw exception
            assertDoesNotThrow(
                () -> manager.dispose(),
                "Trial " + trial + ": Dispose should not throw exception when sound is null"
            );
        }
    }
}
