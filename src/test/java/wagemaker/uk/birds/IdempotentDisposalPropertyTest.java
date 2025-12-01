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
 * Property-based test for idempotent disposal.
 * Feature: bird-ambient-sound, Property 7: Idempotent disposal
 * Validates: Requirements 5.4
 */
public class IdempotentDisposalPropertyTest {
    
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
     * Property 7: Idempotent disposal
     * For any state (sound playing or stopped), calling dispose() should safely clean up
     * resources without throwing exceptions, and calling it multiple times should be safe.
     * Validates: Requirements 5.4
     * 
     * This property-based test runs 100 trials and verifies that:
     * 1. dispose() can be called multiple times without errors
     * 2. Second dispose() call doesn't attempt to stop already-stopped sound
     * 3. Second dispose() call doesn't attempt to dispose already-disposed sound
     * 4. State remains consistent after multiple dispose() calls
     */
    @Test
    public void idempotentDisposalWithSoundPlaying() throws Exception {
        float viewWidth = 800f;
        float viewHeight = 600f;
        
        OrthographicCamera camera = new OrthographicCamera();
        camera.setToOrtho(false, viewWidth, viewHeight);
        Viewport viewport = new FitViewport(viewWidth, viewHeight, camera);
        
        // Run 100 trials
        for (int trial = 0; trial < 100; trial++) {
            Sound mockSound = Mockito.mock(Sound.class);
            long mockSoundId = 14000L + trial;
            when(mockSound.loop()).thenReturn(mockSoundId);
            
            com.badlogic.gdx.graphics.Texture mockTexture = Mockito.mock(com.badlogic.gdx.graphics.Texture.class);
            BirdFormationManager manager = new BirdFormationManager(camera, viewport, mockTexture);
            
            // Inject mock sound
            Field soundField = BirdFormationManager.class.getDeclaredField("birdSound");
            soundField.setAccessible(true);
            soundField.set(manager, mockSound);
            
            Field soundIdField = BirdFormationManager.class.getDeclaredField("birdSoundId");
            soundIdField.setAccessible(true);
            
            // Start sound
            Field nextSpawnIntervalField = BirdFormationManager.class.getDeclaredField("nextSpawnInterval");
            nextSpawnIntervalField.setAccessible(true);
            float interval = nextSpawnIntervalField.getFloat(manager);
            manager.update(interval + 0.1f, 0, 0);
            
            // Verify sound is playing
            assertEquals(mockSoundId, soundIdField.getLong(manager));
            
            // First dispose - should not throw
            assertDoesNotThrow(
                () -> manager.dispose(),
                "Trial " + trial + ": First dispose should not throw exception"
            );
            
            // Verify sound was stopped and disposed once
            verify(mockSound, times(1)).stop(mockSoundId);
            verify(mockSound, times(1)).dispose();
            
            // Verify state after first dispose
            assertEquals(-1L, soundIdField.getLong(manager), "Sound ID should be -1 after first dispose");
            assertNull(soundField.get(manager), "Sound should be null after first dispose");
            
            // Second dispose - should not throw and should not call stop/dispose again
            assertDoesNotThrow(
                () -> manager.dispose(),
                "Trial " + trial + ": Second dispose should not throw exception"
            );
            
            // Verify sound.stop() and sound.dispose() were NOT called again
            verify(mockSound, times(1)).stop(mockSoundId); // Still only 1 time
            verify(mockSound, times(1)).dispose(); // Still only 1 time
            
            // Third dispose - should still be safe
            assertDoesNotThrow(
                () -> manager.dispose(),
                "Trial " + trial + ": Third dispose should not throw exception"
            );
            
            // Verify still only called once
            verify(mockSound, times(1)).stop(mockSoundId);
            verify(mockSound, times(1)).dispose();
        }
    }
    
    /**
     * Property: Idempotent disposal when sound not playing
     * For any manager with sound loaded but not playing, multiple dispose() calls
     * should be safe.
     */
    @Test
    public void idempotentDisposalWithSoundNotPlaying() throws Exception {
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
            assertEquals(-1L, soundIdField.getLong(manager));
            
            // First dispose
            assertDoesNotThrow(
                () -> manager.dispose(),
                "Trial " + trial + ": First dispose should not throw exception"
            );
            
            // Verify sound.stop() was NOT called (sound wasn't playing)
            verify(mockSound, never()).stop(anyLong());
            // Verify sound.dispose() was called once
            verify(mockSound, times(1)).dispose();
            
            // Second dispose
            assertDoesNotThrow(
                () -> manager.dispose(),
                "Trial " + trial + ": Second dispose should not throw exception"
            );
            
            // Verify still not called
            verify(mockSound, never()).stop(anyLong());
            verify(mockSound, times(1)).dispose(); // Still only 1 time
            
            // Third dispose
            assertDoesNotThrow(
                () -> manager.dispose(),
                "Trial " + trial + ": Third dispose should not throw exception"
            );
            
            verify(mockSound, never()).stop(anyLong());
            verify(mockSound, times(1)).dispose();
        }
    }
    
    /**
     * Property: Idempotent disposal with null sound
     * For any manager with null sound, multiple dispose() calls should be safe.
     */
    @Test
    public void idempotentDisposalWithNullSound() throws Exception {
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
            
            // Multiple dispose calls should all be safe
            assertDoesNotThrow(
                () -> {
                    manager.dispose();
                    manager.dispose();
                    manager.dispose();
                },
                "Trial " + trial + ": Multiple dispose calls with null sound should not throw exception"
            );
        }
    }
    
    /**
     * Property: Idempotent disposal after complete lifecycle
     * For any manager that has gone through spawn-despawn cycle, multiple dispose()
     * calls should be safe.
     */
    @Test
    public void idempotentDisposalAfterCompleteLifecycle() throws Exception {
        float viewWidth = 800f;
        float viewHeight = 600f;
        
        OrthographicCamera camera = new OrthographicCamera();
        camera.setToOrtho(false, viewWidth, viewHeight);
        Viewport viewport = new FitViewport(viewWidth, viewHeight, camera);
        
        // Run 50 trials (fewer because each does full lifecycle)
        for (int trial = 0; trial < 50; trial++) {
            Sound mockSound = Mockito.mock(Sound.class);
            long mockSoundId = 15000L + trial;
            when(mockSound.loop()).thenReturn(mockSoundId);
            
            com.badlogic.gdx.graphics.Texture mockTexture = Mockito.mock(com.badlogic.gdx.graphics.Texture.class);
            BirdFormationManager manager = new BirdFormationManager(camera, viewport, mockTexture);
            
            // Inject mock sound
            Field soundField = BirdFormationManager.class.getDeclaredField("birdSound");
            soundField.setAccessible(true);
            soundField.set(manager, mockSound);
            
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
            
            // Wait for fade-out to complete (1+ second)
            for (int i = 0; i < 15; i++) {
                manager.update(0.1f, 0, 0);
            }
            
            // Sound should have been stopped during fade-out
            verify(mockSound, times(1)).stop(mockSoundId);;
            
            // Multiple dispose calls
            assertDoesNotThrow(
                () -> {
                    manager.dispose();
                    manager.dispose();
                    manager.dispose();
                },
                "Trial " + trial + ": Multiple dispose calls after lifecycle should not throw exception"
            );
            
            // Verify sound.stop() still only called once (during despawn, not during dispose)
            verify(mockSound, times(1)).stop(mockSoundId);
            // Verify sound.dispose() called once (during first dispose)
            verify(mockSound, times(1)).dispose();
        }
    }
    
    /**
     * Property: State consistency after multiple dispose calls
     * For any number of dispose() calls, the final state should be consistent
     * (soundId = -1, sound = null).
     */
    @Test
    public void stateConsistencyAfterMultipleDisposeCalls() throws Exception {
        float viewWidth = 800f;
        float viewHeight = 600f;
        
        OrthographicCamera camera = new OrthographicCamera();
        camera.setToOrtho(false, viewWidth, viewHeight);
        Viewport viewport = new FitViewport(viewWidth, viewHeight, camera);
        
        // Run 100 trials
        for (int trial = 0; trial < 100; trial++) {
            Sound mockSound = Mockito.mock(Sound.class);
            long mockSoundId = 16000L + trial;
            when(mockSound.loop()).thenReturn(mockSoundId);
            
            com.badlogic.gdx.graphics.Texture mockTexture = Mockito.mock(com.badlogic.gdx.graphics.Texture.class);
            BirdFormationManager manager = new BirdFormationManager(camera, viewport, mockTexture);
            
            // Inject mock sound
            Field soundField = BirdFormationManager.class.getDeclaredField("birdSound");
            soundField.setAccessible(true);
            soundField.set(manager, mockSound);
            
            Field soundIdField = BirdFormationManager.class.getDeclaredField("birdSoundId");
            soundIdField.setAccessible(true);
            
            // Start sound
            Field nextSpawnIntervalField = BirdFormationManager.class.getDeclaredField("nextSpawnInterval");
            nextSpawnIntervalField.setAccessible(true);
            float interval = nextSpawnIntervalField.getFloat(manager);
            manager.update(interval + 0.1f, 0, 0);
            
            // Call dispose multiple times (random number between 2 and 5)
            int numDisposeCalls = 2 + (trial % 4);
            for (int i = 0; i < numDisposeCalls; i++) {
                manager.dispose();
                
                // After each dispose, verify consistent state
                assertEquals(
                    -1L,
                    soundIdField.getLong(manager),
                    "Trial " + trial + ", Dispose " + i + ": Sound ID should be -1"
                );
                assertNull(
                    soundField.get(manager),
                    "Trial " + trial + ", Dispose " + i + ": Sound should be null"
                );
            }
        }
    }
}
