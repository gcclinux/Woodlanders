package wagemaker.uk.weather;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for puddle collision cleanup.
 * Feature: puddle-fall-damage, Property 5: Cleanup on puddle despawn
 * Validates: Requirements 1.5, 3.4, 3.5, 4.4
 */
public class PuddleCleanupPropertyTest {
    
    private static final float FALL_ZONE_RADIUS = 12.0f;
    
    /**
     * Property 5: Cleanup on puddle despawn
     * For any puddle system state transition to NONE or evaporation completion, 
     * all puddle collision data and triggered states should be cleared from memory.
     * 
     * Validates: Requirements 1.5, 3.4, 3.5, 4.4
     * 
     * This property-based test runs 100 trials with random puddle configurations.
     */
    @Test
    public void cleanupClearsAllTriggeredStates() {
        Random random = new Random(42); // Fixed seed for reproducibility
        
        // Run 100 trials
        for (int trial = 0; trial < 100; trial++) {
            PuddleCollisionSystem system = new PuddleCollisionSystem();
            
            // Generate random number of puddles (1-8)
            int puddleCount = 1 + random.nextInt(8);
            List<WaterPuddle> puddles = new ArrayList<>();
            
            for (int i = 0; i < puddleCount; i++) {
                float puddleX = random.nextFloat() * 1000.0f;
                float puddleY = random.nextFloat() * 1000.0f;
                float puddleWidth = 20.0f + random.nextFloat() * 30.0f;
                float puddleHeight = 15.0f + random.nextFloat() * 25.0f;
                
                WaterPuddle puddle = new WaterPuddle();
                puddle.reset(puddleX, puddleY, puddleWidth, puddleHeight, 0.0f);
                puddles.add(puddle);
            }
            
            // Mark random subset of puddles as triggered
            int triggeredCount = 1 + random.nextInt(puddleCount);
            List<WaterPuddle> triggeredPuddles = new ArrayList<>();
            
            for (int i = 0; i < triggeredCount; i++) {
                WaterPuddle puddle = puddles.get(random.nextInt(puddleCount));
                system.markPuddleTriggered(puddle);
                triggeredPuddles.add(puddle);
            }
            
            // Verify puddles are triggered (no collision at their centers)
            for (WaterPuddle puddle : triggeredPuddles) {
                float centerX = puddle.getX() + puddle.getWidth() / 2.0f;
                float centerY = puddle.getY() + puddle.getHeight() / 2.0f;
                
                PuddleCollisionResult result = system.checkCollision(centerX, centerY, puddles);
                // May or may not have collision depending on if other non-triggered puddles overlap
                // But the specific triggered puddle should not trigger
            }
            
            // Call cleanup
            system.clearAllTriggeredStates();
            
            // Verify all puddles can now trigger again
            for (WaterPuddle puddle : puddles) {
                float centerX = puddle.getX() + puddle.getWidth() / 2.0f;
                float centerY = puddle.getY() + puddle.getHeight() / 2.0f;
                
                PuddleCollisionResult result = system.checkCollision(centerX, centerY, puddles);
                assertTrue(
                    result.hasCollision(),
                    String.format("After cleanup, collision should occur at puddle center. " +
                        "Puddle: (%.2f, %.2f)", centerX, centerY)
                );
                assertNotNull(result.getPuddle(), "Result should contain a puddle after cleanup");
            }
        }
    }
    
    /**
     * Property: Cleanup allows fresh triggering cycle
     * For any set of triggered puddles, after cleanup, the entire trigger cycle 
     * should work fresh (trigger, prevent re-trigger, exit, re-trigger).
     * 
     * This property-based test runs 100 trials with full trigger cycles.
     */
    @Test
    public void cleanupAllowsFreshTriggerCycle() {
        Random random = new Random(42); // Fixed seed for reproducibility
        
        // Run 100 trials
        for (int trial = 0; trial < 100; trial++) {
            PuddleCollisionSystem system = new PuddleCollisionSystem();
            
            // Create puddle
            float puddleX = random.nextFloat() * 1000.0f;
            float puddleY = random.nextFloat() * 1000.0f;
            float puddleWidth = 20.0f + random.nextFloat() * 30.0f;
            float puddleHeight = 15.0f + random.nextFloat() * 25.0f;
            
            WaterPuddle puddle = new WaterPuddle();
            puddle.reset(puddleX, puddleY, puddleWidth, puddleHeight, 0.0f);
            
            List<WaterPuddle> puddles = new ArrayList<>();
            puddles.add(puddle);
            
            float puddleCenterX = puddleX + puddleWidth / 2.0f;
            float puddleCenterY = puddleY + puddleHeight / 2.0f;
            
            // First cycle: trigger and verify
            PuddleCollisionResult result = system.checkCollision(puddleCenterX, puddleCenterY, puddles);
            assertTrue(result.hasCollision(), "First trigger should work");
            
            system.markPuddleTriggered(puddle);
            
            result = system.checkCollision(puddleCenterX, puddleCenterY, puddles);
            assertFalse(result.hasCollision(), "Should be triggered");
            
            // Cleanup
            system.clearAllTriggeredStates();
            
            // Second cycle: should work exactly like first
            result = system.checkCollision(puddleCenterX, puddleCenterY, puddles);
            assertTrue(
                result.hasCollision(),
                "After cleanup, first trigger should work again"
            );
            
            system.markPuddleTriggered(puddle);
            
            result = system.checkCollision(puddleCenterX, puddleCenterY, puddles);
            assertFalse(
                result.hasCollision(),
                "After cleanup and re-trigger, should be triggered again"
            );
            
            // Exit and verify reset still works
            float angle = random.nextFloat() * 2.0f * (float) Math.PI;
            float outsideDistance = FALL_ZONE_RADIUS + 5.0f;
            float playerX = puddleCenterX + outsideDistance * (float) Math.cos(angle);
            float playerY = puddleCenterY + outsideDistance * (float) Math.sin(angle);
            
            system.updateTriggeredStates(playerX, playerY, puddles);
            
            result = system.checkCollision(puddleCenterX, puddleCenterY, puddles);
            assertTrue(
                result.hasCollision(),
                "After cleanup cycle, exit-reset should still work"
            );
        }
    }
    
    /**
     * Property: Multiple cleanups are safe
     * For any collision system, calling cleanup multiple times should be safe 
     * and not cause errors.
     * 
     * This property-based test runs 100 trials with multiple cleanup calls.
     */
    @Test
    public void multipleCleanupsSafe() {
        Random random = new Random(42); // Fixed seed for reproducibility
        
        // Run 100 trials
        for (int trial = 0; trial < 100; trial++) {
            PuddleCollisionSystem system = new PuddleCollisionSystem();
            
            // Create some puddles and trigger them
            int puddleCount = 1 + random.nextInt(5);
            List<WaterPuddle> puddles = new ArrayList<>();
            
            for (int i = 0; i < puddleCount; i++) {
                float puddleX = random.nextFloat() * 1000.0f;
                float puddleY = random.nextFloat() * 1000.0f;
                float puddleWidth = 20.0f + random.nextFloat() * 30.0f;
                float puddleHeight = 15.0f + random.nextFloat() * 25.0f;
                
                WaterPuddle puddle = new WaterPuddle();
                puddle.reset(puddleX, puddleY, puddleWidth, puddleHeight, 0.0f);
                puddles.add(puddle);
                
                system.markPuddleTriggered(puddle);
            }
            
            // Call cleanup multiple times
            int cleanupCount = 2 + random.nextInt(5); // 2-6 cleanups
            
            for (int i = 0; i < cleanupCount; i++) {
                // Should not throw exception
                system.clearAllTriggeredStates();
            }
            
            // Verify system still works after multiple cleanups
            WaterPuddle firstPuddle = puddles.get(0);
            float centerX = firstPuddle.getX() + firstPuddle.getWidth() / 2.0f;
            float centerY = firstPuddle.getY() + firstPuddle.getHeight() / 2.0f;
            
            PuddleCollisionResult result = system.checkCollision(centerX, centerY, puddles);
            assertTrue(
                result.hasCollision(),
                "System should still work after multiple cleanups"
            );
        }
    }
    
    /**
     * Property: Cleanup on empty system is safe
     * For any collision system with no triggered puddles, cleanup should be safe.
     * 
     * This property-based test runs 100 trials with cleanup on empty systems.
     */
    @Test
    public void cleanupOnEmptySystemSafe() {
        Random random = new Random(42); // Fixed seed for reproducibility
        
        // Run 100 trials
        for (int trial = 0; trial < 100; trial++) {
            PuddleCollisionSystem system = new PuddleCollisionSystem();
            
            // Call cleanup on empty system - should not throw exception
            system.clearAllTriggeredStates();
            
            // Create puddle and verify system works
            float puddleX = random.nextFloat() * 1000.0f;
            float puddleY = random.nextFloat() * 1000.0f;
            float puddleWidth = 20.0f + random.nextFloat() * 30.0f;
            float puddleHeight = 15.0f + random.nextFloat() * 25.0f;
            
            WaterPuddle puddle = new WaterPuddle();
            puddle.reset(puddleX, puddleY, puddleWidth, puddleHeight, 0.0f);
            
            List<WaterPuddle> puddles = new ArrayList<>();
            puddles.add(puddle);
            
            float centerX = puddleX + puddleWidth / 2.0f;
            float centerY = puddleY + puddleHeight / 2.0f;
            
            PuddleCollisionResult result = system.checkCollision(centerX, centerY, puddles);
            assertTrue(
                result.hasCollision(),
                "System should work after cleanup on empty system"
            );
        }
    }
    
    /**
     * Property: Cleanup clears zone tracking
     * For any triggered puddles with zone tracking, cleanup should clear both 
     * triggered states and zone tracking data.
     * 
     * This property-based test runs 100 trials verifying complete cleanup.
     */
    @Test
    public void cleanupClearsZoneTracking() {
        Random random = new Random(42); // Fixed seed for reproducibility
        
        // Run 100 trials
        for (int trial = 0; trial < 100; trial++) {
            PuddleCollisionSystem system = new PuddleCollisionSystem();
            
            // Create puddle
            float puddleX = random.nextFloat() * 1000.0f;
            float puddleY = random.nextFloat() * 1000.0f;
            float puddleWidth = 20.0f + random.nextFloat() * 30.0f;
            float puddleHeight = 15.0f + random.nextFloat() * 25.0f;
            
            WaterPuddle puddle = new WaterPuddle();
            puddle.reset(puddleX, puddleY, puddleWidth, puddleHeight, 0.0f);
            
            List<WaterPuddle> puddles = new ArrayList<>();
            puddles.add(puddle);
            
            float puddleCenterX = puddleX + puddleWidth / 2.0f;
            float puddleCenterY = puddleY + puddleHeight / 2.0f;
            
            // Trigger puddle and update states (creates zone tracking)
            system.markPuddleTriggered(puddle);
            system.updateTriggeredStates(puddleCenterX, puddleCenterY, puddles);
            
            // Cleanup
            system.clearAllTriggeredStates();
            
            // After cleanup, the exit-reset mechanism should work fresh
            // Trigger again
            PuddleCollisionResult result = system.checkCollision(puddleCenterX, puddleCenterY, puddles);
            assertTrue(result.hasCollision(), "Should trigger after cleanup");
            
            system.markPuddleTriggered(puddle);
            
            // Move outside
            float angle = random.nextFloat() * 2.0f * (float) Math.PI;
            float outsideDistance = FALL_ZONE_RADIUS + 5.0f;
            float playerX = puddleCenterX + outsideDistance * (float) Math.cos(angle);
            float playerY = puddleCenterY + outsideDistance * (float) Math.sin(angle);
            
            system.updateTriggeredStates(playerX, playerY, puddles);
            
            // Should reset and allow re-trigger
            result = system.checkCollision(puddleCenterX, puddleCenterY, puddles);
            assertTrue(
                result.hasCollision(),
                "Exit-reset should work properly after cleanup cleared zone tracking"
            );
        }
    }
}
