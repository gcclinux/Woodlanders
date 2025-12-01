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
 * Test for sound fade-out functionality.
 */
public class SoundFadeOutTest {
    
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
     * Test that sound volume decreases during fade-out.
     */
    @Test
    public void testSoundFadeOutReducesVolume() throws Exception {
        float viewWidth = 800f;
        float viewHeight = 600f;
        
        OrthographicCamera camera = new OrthographicCamera();
        camera.setToOrtho(false, viewWidth, viewHeight);
        Viewport viewport = new FitViewport(viewWidth, viewHeight, camera);
        
        Sound mockSound = Mockito.mock(Sound.class);
        long mockSoundId = 12345L;
        when(mockSound.loop()).thenReturn(mockSoundId);
        
        com.badlogic.gdx.graphics.Texture mockTexture = Mockito.mock(com.badlogic.gdx.graphics.Texture.class);
        BirdFormationManager manager = new BirdFormationManager(camera, viewport, mockTexture);
        
        // Inject mock sound
        Field soundField = BirdFormationManager.class.getDeclaredField("birdSound");
        soundField.setAccessible(true);
        soundField.set(manager, mockSound);
        
        // Spawn formation to start sound
        Field nextSpawnIntervalField = BirdFormationManager.class.getDeclaredField("nextSpawnInterval");
        nextSpawnIntervalField.setAccessible(true);
        float interval = nextSpawnIntervalField.getFloat(manager);
        manager.update(interval + 0.1f, 0, 0);
        
        // Verify sound started at full volume
        verify(mockSound, times(1)).setVolume(mockSoundId, 1.0f);
        
        // Wait for despawn to trigger fade-out
        float maxUpdateTime = 20f;
        float totalTime = 0f;
        float timeStep = 0.1f;
        
        while (manager.getActiveFormation() != null && totalTime < maxUpdateTime) {
            manager.update(timeStep, 0, 0);
            totalTime += timeStep;
        }
        
        // Formation should be despawned
        assertNull(manager.getActiveFormation());
        
        // Continue updating to process fade-out
        for (int i = 0; i < 15; i++) {
            manager.update(0.1f, 0, 0);
        }
        
        // Verify setVolume was called multiple times with decreasing values
        // (at least once for initial volume, and multiple times during fade)
        verify(mockSound, atLeast(2)).setVolume(eq(mockSoundId), anyFloat());
        
        // Verify sound was eventually stopped
        verify(mockSound, times(1)).stop(mockSoundId);
        
        manager.dispose();
    }
    
    /**
     * Test that fade-out completes within expected duration.
     */
    @Test
    public void testFadeOutCompletesInExpectedTime() throws Exception {
        float viewWidth = 800f;
        float viewHeight = 600f;
        
        OrthographicCamera camera = new OrthographicCamera();
        camera.setToOrtho(false, viewWidth, viewHeight);
        Viewport viewport = new FitViewport(viewWidth, viewHeight, camera);
        
        Sound mockSound = Mockito.mock(Sound.class);
        long mockSoundId = 12345L;
        when(mockSound.loop()).thenReturn(mockSoundId);
        
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
        
        // Wait for despawn
        float maxUpdateTime = 20f;
        float totalTime = 0f;
        float timeStep = 0.1f;
        
        while (manager.getActiveFormation() != null && totalTime < maxUpdateTime) {
            manager.update(timeStep, 0, 0);
            totalTime += timeStep;
        }
        
        // Sound should still be playing (fading out)
        long soundId = soundIdField.getLong(manager);
        assertNotEquals(-1L, soundId, "Sound should still be playing during fade-out");
        
        // Update for fade duration (1 second + buffer)
        for (int i = 0; i < 12; i++) {
            manager.update(0.1f, 0, 0);
        }
        
        // Sound should now be stopped
        soundId = soundIdField.getLong(manager);
        assertEquals(-1L, soundId, "Sound should be stopped after fade-out completes");
        
        manager.dispose();
    }
    
    /**
     * Test that new spawn doesn't occur during fade-out.
     */
    @Test
    public void testNoSpawnDuringFadeOut() throws Exception {
        float viewWidth = 800f;
        float viewHeight = 600f;
        
        OrthographicCamera camera = new OrthographicCamera();
        camera.setToOrtho(false, viewWidth, viewHeight);
        Viewport viewport = new FitViewport(viewWidth, viewHeight, camera);
        
        Sound mockSound = Mockito.mock(Sound.class);
        long mockSoundId = 12345L;
        when(mockSound.loop()).thenReturn(mockSoundId);
        
        com.badlogic.gdx.graphics.Texture mockTexture = Mockito.mock(com.badlogic.gdx.graphics.Texture.class);
        BirdFormationManager manager = new BirdFormationManager(camera, viewport, mockTexture);
        
        // Inject mock sound
        Field soundField = BirdFormationManager.class.getDeclaredField("birdSound");
        soundField.setAccessible(true);
        soundField.set(manager, mockSound);
        
        // Spawn formation
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
        
        // Formation despawned, fade-out in progress
        assertNull(manager.getActiveFormation());
        
        // Update during fade-out period (but not long enough for next spawn)
        for (int i = 0; i < 5; i++) {
            manager.update(0.1f, 0, 0);
        }
        
        // Should still have no formation (fade-out prevents spawn)
        assertNull(manager.getActiveFormation());
        
        // Verify sound.loop() was only called once (no new spawn during fade)
        verify(mockSound, times(1)).loop();
        
        manager.dispose();
    }
}
