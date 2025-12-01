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
 * Property-based test for sound state matching formation state.
 * Feature: bird-ambient-sound, Property 3: Sound state matches formation state
 * Validates: Requirements 1.3, 2.4
 */
public class SoundStateMatchesFormationStatePropertyTest {
    
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
     * Property 3: Sound state matches formation state
     * For any point in time during game execution, the sound should be playing (birdSoundId != -1)
     * if and only if a bird formation is active (activeFormation != null).
     * Validates: Requirements 1.3, 2.4
     * 
     * This property-based test runs 100 trials and verifies that:
     * 1. When formation is null, sound ID is -1 (not playing)
     * 2. When formation is active, sound ID is not -1 (playing)
     * 3. This invariant holds at all times during execution
     */
    @Test
    public void soundStateMatchesFormationState() throws Exception {
        float viewWidth = 800f;
        float viewHeight = 600f;
        
        OrthographicCamera camera = new OrthographicCamera();
        camera.setToOrtho(false, viewWidth, viewHeight);
        Viewport viewport = new FitViewport(viewWidth, viewHeight, camera);
        
        // Run 100 trials
        for (int trial = 0; trial < 100; trial++) {
            Sound mockSound = Mockito.mock(Sound.class);
            long mockSoundId = 3000L + trial;
            when(mockSound.loop()).thenReturn(mockSoundId);
            
            com.badlogic.gdx.graphics.Texture mockTexture = Mockito.mock(com.badlogic.gdx.graphics.Texture.class);
            BirdFormationManager manager = new BirdFormationManager(camera, viewport, mockTexture);
            
            // Inject mock sound
            Field soundField = BirdFormationManager.class.getDeclaredField("birdSound");
            soundField.setAccessible(true);
            soundField.set(manager, mockSound);
            
            Field soundIdField = BirdFormationManager.class.getDeclaredField("birdSoundId");
            soundIdField.setAccessible(true);
            
            // Initial state: no formation, no sound
            assertNull(
                manager.getActiveFormation(),
                "Trial " + trial + ": Initially, formation should be null"
            );
            assertEquals(
                -1L,
                soundIdField.getLong(manager),
                "Trial " + trial + ": Initially, sound should not be playing (soundId == -1)"
            );
            
            // Trigger spawn
            Field nextSpawnIntervalField = BirdFormationManager.class.getDeclaredField("nextSpawnInterval");
            nextSpawnIntervalField.setAccessible(true);
            float interval = nextSpawnIntervalField.getFloat(manager);
            manager.update(interval + 0.1f, 0, 0);
            
            // After spawn: formation exists, sound playing
            BirdFormation formation = manager.getActiveFormation();
            assertNotNull(
                formation,
                "Trial " + trial + ": After spawn, formation should exist"
            );
            long soundId = soundIdField.getLong(manager);
            assertNotEquals(
                -1L,
                soundId,
                "Trial " + trial + ": After spawn, sound should be playing (soundId != -1)"
            );
            
            // During flight: formation exists, sound playing
            for (int i = 0; i < 10; i++) {
                manager.update(0.1f, 0, 0);
                
                BirdFormation currentFormation = manager.getActiveFormation();
                long currentSoundId = soundIdField.getLong(manager);
                
                if (currentFormation != null) {
                    // Formation exists -> sound should be playing
                    assertNotEquals(
                        -1L,
                        currentSoundId,
                        "Trial " + trial + ", Update " + i + ": When formation exists, sound should be playing"
                    );
                } else {
                    // Formation is null -> sound should not be playing
                    assertEquals(
                        -1L,
                        currentSoundId,
                        "Trial " + trial + ", Update " + i + ": When formation is null, sound should not be playing"
                    );
                    break; // Formation despawned, exit loop
                }
            }
            
            // Wait for despawn
            float maxUpdateTime = 20f;
            float totalTime = 0f;
            float timeStep = 0.1f;
            
            while (manager.getActiveFormation() != null && totalTime < maxUpdateTime) {
                manager.update(timeStep, 0, 0);
                totalTime += timeStep;
            }
            
            // After despawn: no formation, no sound
            assertNull(
                manager.getActiveFormation(),
                "Trial " + trial + ": After despawn, formation should be null"
            );
            assertEquals(
                -1L,
                soundIdField.getLong(manager),
                "Trial " + trial + ": After despawn, sound should not be playing (soundId == -1)"
            );
            
            manager.dispose();
        }
    }
    
    /**
     * Property: Sound and formation states are synchronized throughout lifecycle
     * For any complete spawn-despawn cycle, sound state should always match formation state.
     */
    @Test
    public void soundAndFormationStatesSynchronizedThroughoutLifecycle() throws Exception {
        float viewWidth = 800f;
        float viewHeight = 600f;
        
        OrthographicCamera camera = new OrthographicCamera();
        camera.setToOrtho(false, viewWidth, viewHeight);
        Viewport viewport = new FitViewport(viewWidth, viewHeight, camera);
        
        // Run 50 trials (fewer because each does multiple cycles)
        for (int trial = 0; trial < 50; trial++) {
            Sound mockSound = Mockito.mock(Sound.class);
            long mockSoundId = 4000L + trial;
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
                // Before spawn: verify invariant
                BirdFormation beforeFormation = manager.getActiveFormation();
                long beforeSoundId = soundIdField.getLong(manager);
                
                if (beforeFormation == null) {
                    assertEquals(
                        -1L,
                        beforeSoundId,
                        "Trial " + trial + ", Cycle " + cycle + ": Before spawn, if no formation, sound should not be playing"
                    );
                } else {
                    assertNotEquals(
                        -1L,
                        beforeSoundId,
                        "Trial " + trial + ", Cycle " + cycle + ": Before spawn, if formation exists, sound should be playing"
                    );
                }
                
                // Spawn
                Field nextSpawnIntervalField = BirdFormationManager.class.getDeclaredField("nextSpawnInterval");
                nextSpawnIntervalField.setAccessible(true);
                float interval = nextSpawnIntervalField.getFloat(manager);
                manager.update(interval + 0.1f, 0, 0);
                
                // After spawn: verify invariant
                BirdFormation afterSpawnFormation = manager.getActiveFormation();
                long afterSpawnSoundId = soundIdField.getLong(manager);
                
                assertNotNull(
                    afterSpawnFormation,
                    "Trial " + trial + ", Cycle " + cycle + ": After spawn, formation should exist"
                );
                assertNotEquals(
                    -1L,
                    afterSpawnSoundId,
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
                
                // After despawn: verify invariant
                BirdFormation afterDespawnFormation = manager.getActiveFormation();
                long afterDespawnSoundId = soundIdField.getLong(manager);
                
                assertNull(
                    afterDespawnFormation,
                    "Trial " + trial + ", Cycle " + cycle + ": After despawn, formation should be null"
                );
                assertEquals(
                    -1L,
                    afterDespawnSoundId,
                    "Trial " + trial + ", Cycle " + cycle + ": After despawn, sound should not be playing"
                );
            }
            
            manager.dispose();
        }
    }
    
    /**
     * Property: Invariant holds at every update step
     * For any sequence of update calls, the invariant (formation exists <=> sound playing)
     * should hold after every single update.
     */
    @Test
    public void invariantHoldsAtEveryUpdateStep() throws Exception {
        float viewWidth = 800f;
        float viewHeight = 600f;
        
        OrthographicCamera camera = new OrthographicCamera();
        camera.setToOrtho(false, viewWidth, viewHeight);
        Viewport viewport = new FitViewport(viewWidth, viewHeight, camera);
        
        // Run 100 trials
        for (int trial = 0; trial < 100; trial++) {
            Sound mockSound = Mockito.mock(Sound.class);
            long mockSoundId = 6000L + trial;
            when(mockSound.loop()).thenReturn(mockSoundId);
            
            com.badlogic.gdx.graphics.Texture mockTexture = Mockito.mock(com.badlogic.gdx.graphics.Texture.class);
            BirdFormationManager manager = new BirdFormationManager(camera, viewport, mockTexture);
            
            // Inject mock sound
            Field soundField = BirdFormationManager.class.getDeclaredField("birdSound");
            soundField.setAccessible(true);
            soundField.set(manager, mockSound);
            
            Field soundIdField = BirdFormationManager.class.getDeclaredField("birdSoundId");
            soundIdField.setAccessible(true);
            
            // Perform many update steps and check invariant after each
            for (int step = 0; step < 200; step++) {
                manager.update(0.1f, 0, 0);
                
                BirdFormation formation = manager.getActiveFormation();
                long soundId = soundIdField.getLong(manager);
                
                // Check invariant: formation exists <=> sound playing
                if (formation != null) {
                    assertNotEquals(
                        -1L,
                        soundId,
                        "Trial " + trial + ", Step " + step + ": When formation exists, sound must be playing"
                    );
                } else {
                    assertEquals(
                        -1L,
                        soundId,
                        "Trial " + trial + ", Step " + step + ": When formation is null, sound must not be playing"
                    );
                }
            }
            
            manager.dispose();
        }
    }
}
