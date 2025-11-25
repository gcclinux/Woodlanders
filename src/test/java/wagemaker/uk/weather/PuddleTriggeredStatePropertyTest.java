package wagemaker.uk.weather;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for puddle triggered state management.
 * Feature: puddle-fall-damage, Property 9: Triggered state prevents re-trigger
 * Validates: Requirements 4.1, 4.2
 */
public class PuddleTriggeredStatePropertyTest {
    
    private static final float FALL_ZONE_RADIUS = 12.0f;
    
    /**
     * Property 9: Triggered state prevents re-trigger
     * For any puddle marked as triggered, positioning the player within its fall zone 
     * should not trigger another fall sequence.
     * 
     * Validates: Requirements 4.1, 4.2
     * 
     * This property-based test runs 100 trials with random positions.
     */
    @Test
    public void triggeredPuddleDoesNotRetrigger() {
        Random random = new Random(42); // Fixed seed for reproducibility
        
        // Run 100 trials
        for (int trial = 0; trial < 100; trial++) {
            PuddleCollisionSystem system = new PuddleCollisionSystem();
            
            // Generate random puddle position and size
            float puddleX = random.nextFloat() * 1000.0f;
            float puddleY = random.nextFloat() * 1000.0f;
            float puddleWidth = 20.0f + random.nextFloat() * 30.0f;
            float puddleHeight = 15.0f + random.nextFloat() * 25.0f;
            
            // Create puddle
            WaterPuddle puddle = new WaterPuddle();
            puddle.reset(puddleX, puddleY, puddleWidth, puddleHeight, 0.0f);
            
            List<WaterPuddle> puddles = new ArrayList<>();
            puddles.add(puddle);
            
            // Calculate puddle center
            float puddleCenterX = puddleX + puddleWidth / 2.0f;
            float puddleCenterY = puddleY + puddleHeight / 2.0f;
            
            // First collision - should trigger
            PuddleCollisionResult result = system.checkCollision(puddleCenterX, puddleCenterY, puddles);
            assertTrue(
                result.hasCollision(),
                String.format("First collision should occur at puddle center. " +
                    "Puddle center: (%.2f, %.2f)", puddleCenterX, puddleCenterY)
            );
            assertEquals(puddle, result.getPuddle(), "Result should contain the colliding puddle");
            
            // Mark puddle as triggered
            system.markPuddleTriggered(puddle);
            
            // Second collision at same position - should NOT trigger
            result = system.checkCollision(puddleCenterX, puddleCenterY, puddles);
            assertFalse(
                result.hasCollision(),
                String.format("Second collision should NOT occur at same position after marking triggered. " +
                    "Puddle center: (%.2f, %.2f)", puddleCenterX, puddleCenterY)
            );
            assertNull(result.getPuddle(), "Result should not contain a puddle when triggered");
            
            // Try multiple random positions within fall zone - all should NOT trigger
            for (int i = 0; i < 10; i++) {
                float angle = random.nextFloat() * 2.0f * (float) Math.PI;
                float distance = random.nextFloat() * FALL_ZONE_RADIUS;
                float playerX = puddleCenterX + distance * (float) Math.cos(angle);
                float playerY = puddleCenterY + distance * (float) Math.sin(angle);
                
                result = system.checkCollision(playerX, playerY, puddles);
                assertFalse(
                    result.hasCollision(),
                    String.format("Collision should NOT occur at any position within triggered puddle. " +
                        "Player: (%.2f, %.2f), Puddle center: (%.2f, %.2f), Distance: %.2f",
                        playerX, playerY, puddleCenterX, puddleCenterY, distance)
                );
            }
        }
    }
    
    /**
     * Property: Multiple puddles with selective triggering
     * For any configuration of multiple puddles, only triggered puddles should 
     * be excluded from collision detection.
     * 
     * This property-based test runs 100 trials with random puddle configurations.
     */
    @Test
    public void selectiveTriggering() {
        Random random = new Random(42); // Fixed seed for reproducibility
        
        // Run 100 trials
        for (int trial = 0; trial < 100; trial++) {
            PuddleCollisionSystem system = new PuddleCollisionSystem();
            
            // Create 3 puddles at different positions
            List<WaterPuddle> puddles = new ArrayList<>();
            
            for (int i = 0; i < 3; i++) {
                float puddleX = i * 200.0f + random.nextFloat() * 50.0f;
                float puddleY = i * 200.0f + random.nextFloat() * 50.0f;
                float puddleWidth = 20.0f + random.nextFloat() * 30.0f;
                float puddleHeight = 15.0f + random.nextFloat() * 25.0f;
                
                WaterPuddle puddle = new WaterPuddle();
                puddle.reset(puddleX, puddleY, puddleWidth, puddleHeight, 0.0f);
                puddles.add(puddle);
            }
            
            // Mark first puddle as triggered
            system.markPuddleTriggered(puddles.get(0));
            
            // Check collision at first puddle center - should NOT trigger
            WaterPuddle firstPuddle = puddles.get(0);
            float centerX = firstPuddle.getX() + firstPuddle.getWidth() / 2.0f;
            float centerY = firstPuddle.getY() + firstPuddle.getHeight() / 2.0f;
            
            PuddleCollisionResult result = system.checkCollision(centerX, centerY, puddles);
            assertFalse(
                result.hasCollision(),
                "First puddle should not trigger after being marked"
            );
            
            // Check collision at second puddle center - SHOULD trigger
            WaterPuddle secondPuddle = puddles.get(1);
            centerX = secondPuddle.getX() + secondPuddle.getWidth() / 2.0f;
            centerY = secondPuddle.getY() + secondPuddle.getHeight() / 2.0f;
            
            result = system.checkCollision(centerX, centerY, puddles);
            assertTrue(
                result.hasCollision(),
                "Second puddle should still trigger (not marked)"
            );
            assertEquals(secondPuddle, result.getPuddle(), "Result should contain second puddle");
            
            // Mark second puddle as triggered
            system.markPuddleTriggered(secondPuddle);
            
            // Check collision at second puddle center again - should NOT trigger
            result = system.checkCollision(centerX, centerY, puddles);
            assertFalse(
                result.hasCollision(),
                "Second puddle should not trigger after being marked"
            );
            
            // Check collision at third puddle center - SHOULD still trigger
            WaterPuddle thirdPuddle = puddles.get(2);
            centerX = thirdPuddle.getX() + thirdPuddle.getWidth() / 2.0f;
            centerY = thirdPuddle.getY() + thirdPuddle.getHeight() / 2.0f;
            
            result = system.checkCollision(centerX, centerY, puddles);
            assertTrue(
                result.hasCollision(),
                "Third puddle should still trigger (not marked)"
            );
            assertEquals(thirdPuddle, result.getPuddle(), "Result should contain third puddle");
        }
    }
    
    /**
     * Property: Triggered state persists across multiple checks
     * For any triggered puddle, the triggered state should persist across 
     * multiple collision checks until explicitly reset.
     * 
     * This property-based test runs 100 trials with multiple collision checks.
     */
    @Test
    public void triggeredStatePersists() {
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
            
            // Calculate puddle center
            float puddleCenterX = puddleX + puddleWidth / 2.0f;
            float puddleCenterY = puddleY + puddleHeight / 2.0f;
            
            // Mark puddle as triggered
            system.markPuddleTriggered(puddle);
            
            // Perform multiple collision checks - all should return no collision
            int numChecks = 10 + random.nextInt(20); // 10-29 checks
            
            for (int i = 0; i < numChecks; i++) {
                // Random position within fall zone
                float angle = random.nextFloat() * 2.0f * (float) Math.PI;
                float distance = random.nextFloat() * FALL_ZONE_RADIUS;
                float playerX = puddleCenterX + distance * (float) Math.cos(angle);
                float playerY = puddleCenterY + distance * (float) Math.sin(angle);
                
                PuddleCollisionResult result = system.checkCollision(playerX, playerY, puddles);
                assertFalse(
                    result.hasCollision(),
                    String.format("Triggered state should persist across check %d/%d. " +
                        "Player: (%.2f, %.2f), Puddle center: (%.2f, %.2f)",
                        i + 1, numChecks, playerX, playerY, puddleCenterX, puddleCenterY)
                );
            }
        }
    }
}
