package wagemaker.uk.weather;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import wagemaker.uk.player.Player;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for puddle cleanup and collision state management.
 * Tests that collision data is properly cleared when puddles despawn.
 * 
 * Requirements tested:
 * - 1.5: Collision data cleared when rain stops and puddles disappear
 * - 3.4: Collision data removed when puddles evaporate
 * - 3.5: Collision records cleared on NONE state transition
 * - 4.4: Triggered states cleared when puddles despawn
 */
public class PuddleCleanupIntegrationTest {
    
    private static HeadlessApplication application;
    private static ShapeRenderer shapeRenderer;
    private static OrthographicCamera camera;
    
    @BeforeAll
    public static void setUpClass() {
        // Initialize LibGDX headless application
        HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
        application = new HeadlessApplication(new ApplicationAdapter() {}, config);
        
        // Mock GL20 for headless testing
        Gdx.gl = Mockito.mock(GL20.class);
        Gdx.gl20 = Mockito.mock(GL20.class);
        
        // Mock ShapeRenderer
        shapeRenderer = Mockito.mock(ShapeRenderer.class);
        
        camera = new OrthographicCamera(1280, 1024);
        camera.position.set(0, 0, 0);
        camera.update();
    }
    
    @AfterAll
    public static void tearDownClass() {
        if (application != null) {
            application.exit();
        }
    }
    
    @Test
    public void testCleanupOnPuddleEvaporation() {
        // Test that collision data is cleared when puddles evaporate
        // Requirements: 1.5, 3.4, 4.4
        
        PuddleManager puddleManager = new PuddleManager(shapeRenderer);
        puddleManager.initialize();
        
        // Set collision system on puddle manager
        PuddleCollisionSystem collisionSystem = new PuddleCollisionSystem();
        puddleManager.setCollisionSystem(collisionSystem);
        
        Player player = new Player(100f, 100f, camera);
        player.setPuddleManager(puddleManager);
        
        float deltaTime = 0.1f;
        
        // Spawn puddles by simulating rain
        for (int i = 0; i < 60; i++) {
            puddleManager.update(deltaTime, true, 1.0f, camera);
        }
        
        // Verify puddles are active
        assertTrue(puddleManager.getActivePuddleCount() > 0, 
                  "Puddles should be active");
        
        // Get puddle and trigger fall
        List<WaterPuddle> activePuddles = puddleManager.getActivePuddles();
        WaterPuddle testPuddle = activePuddles.get(0);
        float puddleCenterX = testPuddle.getX() + testPuddle.getWidth() / 2.0f;
        float puddleCenterY = testPuddle.getY() + testPuddle.getHeight() / 2.0f;
        
        player.setPosition(puddleCenterX, puddleCenterY);
        
        // Record initial health
        float initialHealth = player.getHealth();
        
        // Trigger fall
        player.update(deltaTime);
        
        // Verify fall was triggered
        float healthAfterFall = player.getHealth();
        assertTrue(healthAfterFall < initialHealth, "Fall should have been triggered");
        
        // Wait for fall animation to complete
        for (int i = 0; i < 45; i++) {
            player.update(deltaTime);
        }
        
        // Stop rain to trigger evaporation
        for (int i = 0; i < 10; i++) {
            puddleManager.update(deltaTime, false, 0.0f, camera);
        }
        
        // Verify puddle system is in EVAPORATING state
        assertEquals(PuddleState.EVAPORATING, puddleManager.getCurrentState(), 
                    "Puddle system should be evaporating");
        
        // Complete evaporation (5 seconds)
        for (int i = 0; i < 50; i++) {
            puddleManager.update(deltaTime, false, 0.0f, camera);
        }
        
        // Verify puddles are gone
        assertEquals(PuddleState.NONE, puddleManager.getCurrentState(), 
                    "Puddle system should be in NONE state");
        assertEquals(0, puddleManager.getActivePuddleCount(), 
                    "No puddles should be active");
        
        // Now spawn puddles again and verify player can fall again
        // (This tests that triggered states were cleared)
        for (int i = 0; i < 60; i++) {
            puddleManager.update(deltaTime, true, 1.0f, camera);
        }
        
        // Get new puddle
        List<WaterPuddle> newActivePuddles = puddleManager.getActivePuddles();
        assertTrue(newActivePuddles.size() > 0, "New puddles should be active");
        
        WaterPuddle newPuddle = newActivePuddles.get(0);
        float newPuddleCenterX = newPuddle.getX() + newPuddle.getWidth() / 2.0f;
        float newPuddleCenterY = newPuddle.getY() + newPuddle.getHeight() / 2.0f;
        
        player.setPosition(newPuddleCenterX, newPuddleCenterY);
        
        float healthBeforeSecondFall = player.getHealth();
        
        // Trigger fall again
        player.update(deltaTime);
        
        // Verify fall was triggered (collision data was cleared)
        float healthAfterSecondFall = player.getHealth();
        assertTrue(healthAfterSecondFall < healthBeforeSecondFall, 
                  "Fall should be triggered again after cleanup");
    }
    
    @Test
    public void testCleanupOnRainStopBeforeThreshold() {
        // Test that collision data is cleared when rain stops before puddles spawn
        // Requirements: 3.5, 4.4
        
        PuddleManager puddleManager = new PuddleManager(shapeRenderer);
        puddleManager.initialize();
        
        PuddleCollisionSystem collisionSystem = new PuddleCollisionSystem();
        puddleManager.setCollisionSystem(collisionSystem);
        
        float deltaTime = 0.1f;
        
        // Start rain but stop before threshold
        for (int i = 0; i < 30; i++) {
            puddleManager.update(deltaTime, true, 1.0f, camera);
        }
        
        // Verify we're in ACCUMULATING state
        assertEquals(PuddleState.ACCUMULATING, puddleManager.getCurrentState(), 
                    "Should be accumulating");
        
        // Stop rain
        puddleManager.update(deltaTime, false, 0.0f, camera);
        
        // Verify transition to NONE
        assertEquals(PuddleState.NONE, puddleManager.getCurrentState(), 
                    "Should transition to NONE");
        
        // Verify no puddles spawned
        assertEquals(0, puddleManager.getActivePuddleCount(), 
                    "No puddles should have spawned");
        
        // System should be in clean state
        // Start rain again and verify it works normally
        for (int i = 0; i < 60; i++) {
            puddleManager.update(deltaTime, true, 1.0f, camera);
        }
        
        assertTrue(puddleManager.getActivePuddleCount() > 0, 
                  "Puddles should spawn after cleanup");
    }
    
    @Test
    public void testMultipleCyclesNoMemoryLeak() {
        // Test that multiple spawn/despawn cycles don't cause memory leaks
        // Requirements: 1.5, 3.4, 3.5, 4.4
        
        PuddleManager puddleManager = new PuddleManager(shapeRenderer);
        puddleManager.initialize();
        
        PuddleCollisionSystem collisionSystem = new PuddleCollisionSystem();
        puddleManager.setCollisionSystem(collisionSystem);
        
        Player player = new Player(100f, 100f, camera);
        player.setPuddleManager(puddleManager);
        
        float deltaTime = 0.1f;
        
        // Run multiple spawn/despawn cycles
        for (int cycle = 0; cycle < 5; cycle++) {
            // Spawn puddles
            for (int i = 0; i < 60; i++) {
                puddleManager.update(deltaTime, true, 1.0f, camera);
            }
            
            assertTrue(puddleManager.getActivePuddleCount() > 0, 
                      "Puddles should spawn in cycle " + cycle);
            
            // Trigger fall if possible
            List<WaterPuddle> activePuddles = puddleManager.getActivePuddles();
            if (!activePuddles.isEmpty()) {
                WaterPuddle testPuddle = activePuddles.get(0);
                float puddleCenterX = testPuddle.getX() + testPuddle.getWidth() / 2.0f;
                float puddleCenterY = testPuddle.getY() + testPuddle.getHeight() / 2.0f;
                
                player.setPosition(puddleCenterX, puddleCenterY);
                player.update(deltaTime);
                
                // Wait for animation
                for (int i = 0; i < 45; i++) {
                    player.update(deltaTime);
                }
            }
            
            // Evaporate puddles
            for (int i = 0; i < 60; i++) {
                puddleManager.update(deltaTime, false, 0.0f, camera);
            }
            
            assertEquals(PuddleState.NONE, puddleManager.getCurrentState(), 
                        "Should be in NONE state after cycle " + cycle);
            assertEquals(0, puddleManager.getActivePuddleCount(), 
                        "No puddles should remain after cycle " + cycle);
        }
        
        // After all cycles, system should still work normally
        for (int i = 0; i < 60; i++) {
            puddleManager.update(deltaTime, true, 1.0f, camera);
        }
        
        assertTrue(puddleManager.getActivePuddleCount() > 0, 
                  "Puddles should still spawn after multiple cycles");
    }
    
    @Test
    public void testCleanupClearsTriggeredStates() {
        // Test that triggered states are cleared when puddles despawn
        // Requirements: 4.4
        
        PuddleManager puddleManager = new PuddleManager(shapeRenderer);
        puddleManager.initialize();
        
        PuddleCollisionSystem collisionSystem = new PuddleCollisionSystem();
        puddleManager.setCollisionSystem(collisionSystem);
        
        Player player = new Player(100f, 100f, camera);
        player.setPuddleManager(puddleManager);
        
        float deltaTime = 0.1f;
        
        // Spawn puddles
        for (int i = 0; i < 60; i++) {
            puddleManager.update(deltaTime, true, 1.0f, camera);
        }
        
        // Get puddle and trigger fall
        List<WaterPuddle> activePuddles = puddleManager.getActivePuddles();
        WaterPuddle testPuddle = activePuddles.get(0);
        String puddleId = testPuddle.getId();
        float puddleCenterX = testPuddle.getX() + testPuddle.getWidth() / 2.0f;
        float puddleCenterY = testPuddle.getY() + testPuddle.getHeight() / 2.0f;
        
        player.setPosition(puddleCenterX, puddleCenterY);
        
        float healthBefore = player.getHealth();
        
        // Trigger fall
        player.update(deltaTime);
        
        float healthAfter = player.getHealth();
        assertTrue(healthAfter < healthBefore, "Fall should be triggered");
        
        // Wait for animation
        for (int i = 0; i < 45; i++) {
            player.update(deltaTime);
        }
        
        // Player is still in puddle, try to fall again (should not work)
        float healthBeforeSecondAttempt = player.getHealth();
        player.update(deltaTime);
        
        assertEquals(healthBeforeSecondAttempt, player.getHealth(), 0.01f, 
                    "Should not fall again in same puddle");
        
        // Now evaporate puddles
        for (int i = 0; i < 60; i++) {
            puddleManager.update(deltaTime, false, 0.0f, camera);
        }
        
        assertEquals(PuddleState.NONE, puddleManager.getCurrentState(), 
                    "Puddles should be gone");
        
        // Spawn new puddles
        for (int i = 0; i < 60; i++) {
            puddleManager.update(deltaTime, true, 1.0f, camera);
        }
        
        // Get new puddle at same position (or close to it)
        List<WaterPuddle> newPuddles = puddleManager.getActivePuddles();
        WaterPuddle newPuddle = newPuddles.get(0);
        float newPuddleCenterX = newPuddle.getX() + newPuddle.getWidth() / 2.0f;
        float newPuddleCenterY = newPuddle.getY() + newPuddle.getHeight() / 2.0f;
        
        player.setPosition(newPuddleCenterX, newPuddleCenterY);
        
        float healthBeforeNewFall = player.getHealth();
        
        // Trigger fall in new puddle
        player.update(deltaTime);
        
        // Should be able to fall again (triggered states were cleared)
        float healthAfterNewFall = player.getHealth();
        assertTrue(healthAfterNewFall < healthBeforeNewFall, 
                  "Should be able to fall in new puddle after cleanup");
    }
    
    @Test
    public void testCleanupOnStateTransitionToNone() {
        // Test that cleanup occurs on any transition to NONE state
        // Requirements: 3.5
        
        PuddleManager puddleManager = new PuddleManager(shapeRenderer);
        puddleManager.initialize();
        
        PuddleCollisionSystem collisionSystem = new PuddleCollisionSystem();
        puddleManager.setCollisionSystem(collisionSystem);
        
        float deltaTime = 0.1f;
        
        // Test 1: ACCUMULATING -> NONE (rain stops before threshold)
        for (int i = 0; i < 30; i++) {
            puddleManager.update(deltaTime, true, 1.0f, camera);
        }
        
        assertEquals(PuddleState.ACCUMULATING, puddleManager.getCurrentState());
        
        puddleManager.update(deltaTime, false, 0.0f, camera);
        
        assertEquals(PuddleState.NONE, puddleManager.getCurrentState(), 
                    "Should transition to NONE from ACCUMULATING");
        
        // Test 2: EVAPORATING -> NONE (evaporation completes)
        for (int i = 0; i < 60; i++) {
            puddleManager.update(deltaTime, true, 1.0f, camera);
        }
        
        assertEquals(PuddleState.ACTIVE, puddleManager.getCurrentState());
        
        for (int i = 0; i < 60; i++) {
            puddleManager.update(deltaTime, false, 0.0f, camera);
        }
        
        assertEquals(PuddleState.NONE, puddleManager.getCurrentState(), 
                    "Should transition to NONE from EVAPORATING");
        
        // Verify system is in clean state
        assertEquals(0, puddleManager.getActivePuddleCount(), 
                    "No puddles should remain");
    }
    
    @Test
    public void testNoMemoryLeakWithManyFalls() {
        // Test that many fall events don't cause memory leaks in collision system
        // Requirements: 1.5, 4.4
        
        PuddleManager puddleManager = new PuddleManager(shapeRenderer);
        puddleManager.initialize();
        
        PuddleCollisionSystem collisionSystem = new PuddleCollisionSystem();
        puddleManager.setCollisionSystem(collisionSystem);
        
        Player player = new Player(100f, 100f, camera);
        player.setPuddleManager(puddleManager);
        
        float deltaTime = 0.1f;
        
        // Spawn puddles
        for (int i = 0; i < 60; i++) {
            puddleManager.update(deltaTime, true, 1.0f, camera);
        }
        
        List<WaterPuddle> activePuddles = puddleManager.getActivePuddles();
        
        // Trigger falls in multiple puddles
        for (int puddleIndex = 0; puddleIndex < Math.min(3, activePuddles.size()); puddleIndex++) {
            WaterPuddle puddle = activePuddles.get(puddleIndex);
            float puddleCenterX = puddle.getX() + puddle.getWidth() / 2.0f;
            float puddleCenterY = puddle.getY() + puddle.getHeight() / 2.0f;
            
            player.setPosition(puddleCenterX, puddleCenterY);
            player.update(deltaTime);
            
            // Wait for animation
            for (int i = 0; i < 45; i++) {
                player.update(deltaTime);
            }
        }
        
        // Evaporate puddles
        for (int i = 0; i < 60; i++) {
            puddleManager.update(deltaTime, false, 0.0f, camera);
        }
        
        // Verify cleanup
        assertEquals(PuddleState.NONE, puddleManager.getCurrentState());
        assertEquals(0, puddleManager.getActivePuddleCount());
        
        // System should still work normally
        for (int i = 0; i < 60; i++) {
            puddleManager.update(deltaTime, true, 1.0f, camera);
        }
        
        assertTrue(puddleManager.getActivePuddleCount() > 0, 
                  "System should work normally after many falls");
    }
}
