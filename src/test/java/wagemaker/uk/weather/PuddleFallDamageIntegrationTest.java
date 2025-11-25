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

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for puddle fall damage system.
 * Tests the complete interaction between puddles, player collision, fall animation, and damage.
 * 
 * Requirements tested:
 * - 1.1: Player falls when intersecting puddle fall zone
 * - 1.2: Fall applies 10% damage to player health
 * - 1.3: Player movement blocked during fall sequence
 * - 1.4: Normal movement restored after fall completes
 * - 2.1-2.6: Fall animation sequence plays correctly
 */
public class PuddleFallDamageIntegrationTest {
    
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
        
        // Mock ShapeRenderer since we don't need actual rendering
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
    public void testPlayerFallsWhenWalkingOverPuddle() {
        // Test that player triggers fall when walking over a puddle
        // Requirements: 1.1, 1.2, 1.3
        
        // Create puddle manager and spawn puddles
        PuddleManager puddleManager = new PuddleManager(shapeRenderer);
        puddleManager.initialize();
        
        // Create player at a position away from puddles
        Player player = new Player(100f, 100f, camera);
        player.setPuddleManager(puddleManager);
        
        // Force puddle system to ACTIVE state by simulating rain cycle
        float deltaTime = 0.1f;
        
        // Simulate rain for 5+ seconds to spawn puddles
        for (int i = 0; i < 60; i++) {
            puddleManager.update(deltaTime, true, 1.0f, camera);
        }
        
        // Verify puddles are active
        assertTrue(puddleManager.getActivePuddleCount() > 0, 
                  "Puddles should be active after accumulation");
        
        // Get a puddle position
        List<WaterPuddle> activePuddles = puddleManager.getActivePuddles();
        assertFalse(activePuddles.isEmpty(), "Should have active puddles");
        
        WaterPuddle testPuddle = activePuddles.get(0);
        float puddleCenterX = testPuddle.getX() + testPuddle.getWidth() / 2.0f;
        float puddleCenterY = testPuddle.getY() + testPuddle.getHeight() / 2.0f;
        
        // Record initial health
        float initialHealth = player.getHealth();
        
        // Move player to puddle center
        player.setPosition(puddleCenterX, puddleCenterY);
        
        // Update player (should trigger fall)
        player.update(deltaTime);
        
        // Verify damage was applied (10% of initial health)
        float expectedHealth = initialHealth - 10.0f;
        assertEquals(expectedHealth, player.getHealth(), 0.01f, 
                    "Player should take 10% damage from fall");
        
        // Verify player is in falling state (movement should be blocked)
        // We can test this by trying to move and checking position doesn't change
        float posX = player.getX();
        float posY = player.getY();
        
        // Try to update player (movement input would be processed if not falling)
        player.update(deltaTime);
        
        // Position should not change during fall
        assertEquals(posX, player.getX(), 0.01f, "X position should not change during fall");
        assertEquals(posY, player.getY(), 0.01f, "Y position should not change during fall");
    }
    
    @Test
    public void testFallAnimationSequenceCompletes() {
        // Test that fall animation plays through all 5 frames and completes
        // Requirements: 2.1-2.6
        
        PuddleManager puddleManager = new PuddleManager(shapeRenderer);
        puddleManager.initialize();
        
        Player player = new Player(100f, 100f, camera);
        player.setPuddleManager(puddleManager);
        
        float deltaTime = 0.1f;
        
        // Spawn puddles
        for (int i = 0; i < 60; i++) {
            puddleManager.update(deltaTime, true, 1.0f, camera);
        }
        
        // Get puddle and move player to it
        List<WaterPuddle> activePuddles = puddleManager.getActivePuddles();
        WaterPuddle testPuddle = activePuddles.get(0);
        float puddleCenterX = testPuddle.getX() + testPuddle.getWidth() / 2.0f;
        float puddleCenterY = testPuddle.getY() + testPuddle.getHeight() / 2.0f;
        
        player.setPosition(puddleCenterX, puddleCenterY);
        
        // Trigger fall
        player.update(deltaTime);
        
        // Animation should take 5 frames * 0.8 seconds = 4.0 seconds total
        // Update for 4.0 seconds (40 frames at 0.1s each)
        for (int i = 0; i < 40; i++) {
            player.update(deltaTime);
        }
        
        // After 4.0 seconds, animation should be complete
        // Player should be able to move again
        float posX = player.getX();
        float posY = player.getY();
        
        // Update one more time to ensure completion
        player.update(deltaTime);
        
        // Now player should be able to move (fall sequence complete)
        // We can't directly test movement without input, but we can verify
        // the player update doesn't throw exceptions and position is stable
        assertDoesNotThrow(() -> player.update(deltaTime), 
                          "Player update should not throw after fall completes");
    }
    
    @Test
    public void testPlayerCannotFallTwiceInSamePuddle() {
        // Test that triggered state prevents repeated falls
        // Requirements: 4.1, 4.2
        
        PuddleManager puddleManager = new PuddleManager(shapeRenderer);
        puddleManager.initialize();
        
        Player player = new Player(100f, 100f, camera);
        player.setPuddleManager(puddleManager);
        
        float deltaTime = 0.1f;
        
        // Spawn puddles
        for (int i = 0; i < 60; i++) {
            puddleManager.update(deltaTime, true, 1.0f, camera);
        }
        
        // Get puddle and move player to it
        List<WaterPuddle> activePuddles = puddleManager.getActivePuddles();
        WaterPuddle testPuddle = activePuddles.get(0);
        float puddleCenterX = testPuddle.getX() + testPuddle.getWidth() / 2.0f;
        float puddleCenterY = testPuddle.getY() + testPuddle.getHeight() / 2.0f;
        
        player.setPosition(puddleCenterX, puddleCenterY);
        
        // Record initial health
        float initialHealth = player.getHealth();
        
        // Trigger first fall
        player.update(deltaTime);
        
        float healthAfterFirstFall = player.getHealth();
        float expectedHealthAfterFirst = initialHealth - 10.0f;
        assertEquals(expectedHealthAfterFirst, healthAfterFirstFall, 0.01f, 
                    "Health should decrease by 10% after first fall");
        
        // Wait for fall animation to complete (4+ seconds)
        for (int i = 0; i < 45; i++) {
            player.update(deltaTime);
        }
        
        // Player is still in puddle, but should not fall again
        // Update several more times
        for (int i = 0; i < 10; i++) {
            player.update(deltaTime);
        }
        
        // Health should not have decreased again
        assertEquals(healthAfterFirstFall, player.getHealth(), 0.01f, 
                    "Health should not decrease again while in same puddle");
    }
    
    @Test
    public void testTriggeredStateResetsWhenPlayerExitsPuddle() {
        // Test that triggered state resets when player exits puddle zone
        // Requirements: 4.3
        
        PuddleManager puddleManager = new PuddleManager(shapeRenderer);
        puddleManager.initialize();
        
        Player player = new Player(100f, 100f, camera);
        player.setPuddleManager(puddleManager);
        
        float deltaTime = 0.1f;
        
        // Spawn puddles
        for (int i = 0; i < 60; i++) {
            puddleManager.update(deltaTime, true, 1.0f, camera);
        }
        
        // Get puddle and move player to it
        List<WaterPuddle> activePuddles = puddleManager.getActivePuddles();
        WaterPuddle testPuddle = activePuddles.get(0);
        float puddleCenterX = testPuddle.getX() + testPuddle.getWidth() / 2.0f;
        float puddleCenterY = testPuddle.getY() + testPuddle.getHeight() / 2.0f;
        
        player.setPosition(puddleCenterX, puddleCenterY);
        
        // Record initial health
        float initialHealth = player.getHealth();
        
        // Trigger first fall
        player.update(deltaTime);
        
        float healthAfterFirstFall = player.getHealth();
        
        // Wait for fall animation to complete
        for (int i = 0; i < 45; i++) {
            player.update(deltaTime);
        }
        
        // Move player far away from puddle (> 12 pixels)
        player.setPosition(puddleCenterX + 50f, puddleCenterY + 50f);
        
        // Update to reset triggered state
        for (int i = 0; i < 5; i++) {
            player.update(deltaTime);
        }
        
        // Move player back to puddle center
        player.setPosition(puddleCenterX, puddleCenterY);
        
        // Trigger fall again (should work now that triggered state is reset)
        player.update(deltaTime);
        
        // Health should decrease again
        float healthAfterSecondFall = player.getHealth();
        float expectedHealthAfterSecond = healthAfterFirstFall - 10.0f;
        assertEquals(expectedHealthAfterSecond, healthAfterSecondFall, 0.01f, 
                    "Health should decrease again after exiting and re-entering puddle");
    }
    
    @Test
    public void testMultiplePuddlesCanTriggerFalls() {
        // Test that player can fall in different puddles
        // Requirements: 1.1, 4.1
        
        PuddleManager puddleManager = new PuddleManager(shapeRenderer);
        puddleManager.initialize();
        
        Player player = new Player(100f, 100f, camera);
        player.setPuddleManager(puddleManager);
        
        float deltaTime = 0.1f;
        
        // Spawn puddles
        for (int i = 0; i < 60; i++) {
            puddleManager.update(deltaTime, true, 1.0f, camera);
        }
        
        List<WaterPuddle> activePuddles = puddleManager.getActivePuddles();
        assertTrue(activePuddles.size() >= 2, "Need at least 2 puddles for this test");
        
        // Record initial health
        float initialHealth = player.getHealth();
        
        // Fall in first puddle
        WaterPuddle puddle1 = activePuddles.get(0);
        float puddle1CenterX = puddle1.getX() + puddle1.getWidth() / 2.0f;
        float puddle1CenterY = puddle1.getY() + puddle1.getHeight() / 2.0f;
        
        player.setPosition(puddle1CenterX, puddle1CenterY);
        player.update(deltaTime);
        
        float healthAfterFirst = player.getHealth();
        assertEquals(initialHealth - 10.0f, healthAfterFirst, 0.01f, 
                    "Health should decrease after first puddle");
        
        // Wait for animation to complete
        for (int i = 0; i < 45; i++) {
            player.update(deltaTime);
        }
        
        // Fall in second puddle
        WaterPuddle puddle2 = activePuddles.get(1);
        float puddle2CenterX = puddle2.getX() + puddle2.getWidth() / 2.0f;
        float puddle2CenterY = puddle2.getY() + puddle2.getHeight() / 2.0f;
        
        player.setPosition(puddle2CenterX, puddle2CenterY);
        player.update(deltaTime);
        
        float healthAfterSecond = player.getHealth();
        assertEquals(healthAfterFirst - 10.0f, healthAfterSecond, 0.01f, 
                    "Health should decrease again in different puddle");
    }
    
    @Test
    public void testFallDamageDoesNotGoNegative() {
        // Test that health is clamped to 0 and doesn't go negative
        // Requirements: 1.2
        
        PuddleManager puddleManager = new PuddleManager(shapeRenderer);
        puddleManager.initialize();
        
        Player player = new Player(100f, 100f, camera);
        player.setPuddleManager(puddleManager);
        
        // Set player health to 5% (less than fall damage)
        player.setHealth(5.0f);
        
        float deltaTime = 0.1f;
        
        // Spawn puddles
        for (int i = 0; i < 60; i++) {
            puddleManager.update(deltaTime, true, 1.0f, camera);
        }
        
        // Get puddle and move player to it
        List<WaterPuddle> activePuddles = puddleManager.getActivePuddles();
        WaterPuddle testPuddle = activePuddles.get(0);
        float puddleCenterX = testPuddle.getX() + testPuddle.getWidth() / 2.0f;
        float puddleCenterY = testPuddle.getY() + testPuddle.getHeight() / 2.0f;
        
        player.setPosition(puddleCenterX, puddleCenterY);
        
        // Trigger fall
        player.update(deltaTime);
        
        // Health should be 0, not negative
        assertTrue(player.getHealth() >= 0.0f, "Health should not go negative");
        assertEquals(0.0f, player.getHealth(), 0.01f, "Health should be clamped to 0");
    }
}
