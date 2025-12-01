package wagemaker.uk.birds;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Property-based test for null sound safety.
 * Feature: bird-ambient-sound, Property 5: Null sound safety
 * Validates: Requirements 4.2, 4.3
 */
public class NullSoundSafetyPropertyTest {
    
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
     * Property 5: Null sound safety
     * For any operation (spawn, despawn, update, dispose), if the sound object is null,
     * no exceptions should be thrown and the system should continue normally.
     * Validates: Requirements 4.2, 4.3
     * 
     * This property-based test runs 100 trials and verifies that:
     * 1. When sound is null, spawn operations complete without exceptions
     * 2. When sound is null, despawn operations complete without exceptions
     * 3. When sound is null, update operations complete without exceptions
     * 4. When sound is null, dispose operations complete without exceptions
     * 5. Visual bird system continues to function normally without sound
     */
    @Test
    public void nullSoundSafetyDuringAllOperations() {
        float viewWidth = 800f;
        float viewHeight = 600f;
        
        OrthographicCamera camera = new OrthographicCamera();
        camera.setToOrtho(false, viewWidth, viewHeight);
        Viewport viewport = new FitViewport(viewWidth, viewHeight, camera);
        
        // Run 100 trials
        for (int trial = 0; trial < 100; trial++) {
            Texture mockTexture = Mockito.mock(Texture.class);
            BirdFormationManager manager = new BirdFormationManager(camera, viewport, mockTexture);
            
            // Note: Sound is null because we're using the test constructor
            // which doesn't call initialize() that would load the sound
            
            // Test spawn operation with null sound - should not throw exception
            assertDoesNotThrow(
                () -> {
                    float interval = manager.getNextSpawnInterval();
                    manager.update(interval + 0.1f, 0, 0);
                },
                "Trial " + trial + ": Spawn operation should not throw exception when sound is null"
            );
            
            // Verify formation was spawned despite null sound
            BirdFormation formation = manager.getActiveFormation();
            assertNotNull(
                formation,
                "Trial " + trial + ": Formation should spawn even when sound is null"
            );
            
            // Test update operations with null sound - should not throw exception
            assertDoesNotThrow(
                () -> {
                    for (int i = 0; i < 10; i++) {
                        manager.update(0.1f, 0, 0);
                    }
                },
                "Trial " + trial + ": Update operations should not throw exception when sound is null"
            );
            
            // Test despawn operation with null sound - should not throw exception
            assertDoesNotThrow(
                () -> {
                    float maxUpdateTime = 20f;
                    float totalTime = 0f;
                    float timeStep = 0.1f;
                    
                    while (manager.getActiveFormation() != null && totalTime < maxUpdateTime) {
                        manager.update(timeStep, 0, 0);
                        totalTime += timeStep;
                    }
                },
                "Trial " + trial + ": Despawn operation should not throw exception when sound is null"
            );
            
            // Verify formation was despawned
            assertNull(
                manager.getActiveFormation(),
                "Trial " + trial + ": Formation should despawn even when sound is null"
            );
            
            // Test dispose operation with null sound - should not throw exception
            assertDoesNotThrow(
                () -> manager.dispose(),
                "Trial " + trial + ": Dispose operation should not throw exception when sound is null"
            );
        }
    }
    
    /**
     * Property: Multiple spawn-despawn cycles with null sound
     * For any sequence of spawn-despawn cycles with null sound, the system
     * should continue functioning normally without exceptions.
     */
    @Test
    public void multipleSpawnDespawnCyclesWithNullSound() {
        float viewWidth = 800f;
        float viewHeight = 600f;
        
        OrthographicCamera camera = new OrthographicCamera();
        camera.setToOrtho(false, viewWidth, viewHeight);
        Viewport viewport = new FitViewport(viewWidth, viewHeight, camera);
        
        // Run 50 trials (fewer because each trial does multiple cycles)
        for (int trial = 0; trial < 50; trial++) {
            Texture mockTexture = Mockito.mock(Texture.class);
            BirdFormationManager manager = new BirdFormationManager(camera, viewport, mockTexture);
            
            // Perform 3 spawn-despawn cycles with null sound
            for (int cycle = 0; cycle < 3; cycle++) {
                // Spawn
                assertDoesNotThrow(
                    () -> {
                        float interval = manager.getNextSpawnInterval();
                        manager.update(interval + 0.1f, 0, 0);
                    },
                    "Trial " + trial + ", Cycle " + cycle + ": Spawn should not throw with null sound"
                );
                
                assertNotNull(
                    manager.getActiveFormation(),
                    "Trial " + trial + ", Cycle " + cycle + ": Formation should spawn with null sound"
                );
                
                // Despawn
                assertDoesNotThrow(
                    () -> {
                        float maxUpdateTime = 20f;
                        float totalTime = 0f;
                        float timeStep = 0.1f;
                        
                        while (manager.getActiveFormation() != null && totalTime < maxUpdateTime) {
                            manager.update(timeStep, 0, 0);
                            totalTime += timeStep;
                        }
                    },
                    "Trial " + trial + ", Cycle " + cycle + ": Despawn should not throw with null sound"
                );
                
                assertNull(
                    manager.getActiveFormation(),
                    "Trial " + trial + ", Cycle " + cycle + ": Formation should despawn with null sound"
                );
            }
            
            // Final dispose
            assertDoesNotThrow(
                () -> manager.dispose(),
                "Trial " + trial + ": Final dispose should not throw with null sound"
            );
        }
    }
    
    /**
     * Property: Dispose with null sound is safe
     * For any manager state with null sound, calling dispose() should be safe
     * and not throw exceptions.
     */
    @Test
    public void disposeWithNullSoundIsSafe() {
        float viewWidth = 800f;
        float viewHeight = 600f;
        
        OrthographicCamera camera = new OrthographicCamera();
        camera.setToOrtho(false, viewWidth, viewHeight);
        Viewport viewport = new FitViewport(viewWidth, viewHeight, camera);
        
        // Run 100 trials
        for (int trial = 0; trial < 100; trial++) {
            Texture mockTexture = Mockito.mock(Texture.class);
            BirdFormationManager manager = new BirdFormationManager(camera, viewport, mockTexture);
            
            // Randomly decide whether to spawn before disposing
            boolean shouldSpawn = (trial % 2 == 0);
            
            if (shouldSpawn) {
                float interval = manager.getNextSpawnInterval();
                manager.update(interval + 0.1f, 0, 0);
                assertNotNull(manager.getActiveFormation());
            }
            
            // Dispose should not throw exception even with null sound
            assertDoesNotThrow(
                () -> manager.dispose(),
                "Trial " + trial + ": Dispose should not throw exception when sound is null"
            );
        }
    }
}
