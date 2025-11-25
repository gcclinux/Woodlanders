package wagemaker.uk.weather;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for puddle collision detection.
 * Feature: puddle-fall-damage, Property 1: Collision triggers fall
 * Validates: Requirements 1.1, 3.2
 */
public class PuddleCollisionPropertyTest {
    
    private static final float FALL_ZONE_RADIUS = 12.0f;
    
    /**
     * Property 1: Collision triggers fall
     * For any player position and puddle position, when the distance from player center 
     * to puddle center is less than or equal to 12 pixels and the puddle is not triggered, 
     * the fall mechanic should activate.
     * 
     * Validates: Requirements 1.1, 3.2
     * 
     * This property-based test runs 100 trials with random positions.
     */
    @Test
    public void collisionTriggersWhenWithinFallZone() {
        Random random = new Random(42); // Fixed seed for reproducibility
        
        // Run 100 trials
        for (int trial = 0; trial < 100; trial++) {
            PuddleCollisionSystem system = new PuddleCollisionSystem();
            
            // Generate random puddle position and size
            float puddleX = random.nextFloat() * 1000.0f;
            float puddleY = random.nextFloat() * 1000.0f;
            float puddleWidth = 20.0f + random.nextFloat() * 30.0f; // 20-50 pixels
            float puddleHeight = 15.0f + random.nextFloat() * 25.0f; // 15-40 pixels
            
            // Create puddle
            WaterPuddle puddle = new WaterPuddle();
            puddle.reset(puddleX, puddleY, puddleWidth, puddleHeight, 0.0f);
            
            List<WaterPuddle> puddles = new ArrayList<>();
            puddles.add(puddle);
            
            // Calculate puddle center
            float puddleCenterX = puddleX + puddleWidth / 2.0f;
            float puddleCenterY = puddleY + puddleHeight / 2.0f;
            
            // Test Case 1: Player exactly at puddle center (distance = 0)
            PuddleCollisionResult result = system.checkCollision(puddleCenterX, puddleCenterY, puddles);
            assertTrue(
                result.hasCollision(),
                String.format("Collision should occur when player is at puddle center (distance = 0). " +
                    "Puddle center: (%.2f, %.2f)", puddleCenterX, puddleCenterY)
            );
            assertEquals(puddle, result.getPuddle(), "Result should contain the colliding puddle");
            
            // Test Case 2: Player just inside fall zone boundary (distance slightly < 12)
            // Use 11.9 to avoid floating-point precision issues at exact boundary
            float angle = random.nextFloat() * 2.0f * (float) Math.PI;
            float playerX = puddleCenterX + 11.9f * (float) Math.cos(angle);
            float playerY = puddleCenterY + 11.9f * (float) Math.sin(angle);
            
            result = system.checkCollision(playerX, playerY, puddles);
            assertTrue(
                result.hasCollision(),
                String.format("Collision should occur when player is just inside fall zone boundary (distance = 11.9). " +
                    "Player: (%.2f, %.2f), Puddle center: (%.2f, %.2f)",
                    playerX, playerY, puddleCenterX, puddleCenterY)
            );
            
            // Test Case 3: Player just inside fall zone (distance < 12)
            float insideDistance = random.nextFloat() * (FALL_ZONE_RADIUS - 0.1f); // 0 to 11.9
            angle = random.nextFloat() * 2.0f * (float) Math.PI;
            playerX = puddleCenterX + insideDistance * (float) Math.cos(angle);
            playerY = puddleCenterY + insideDistance * (float) Math.sin(angle);
            
            result = system.checkCollision(playerX, playerY, puddles);
            assertTrue(
                result.hasCollision(),
                String.format("Collision should occur when player is inside fall zone (distance = %.2f). " +
                    "Player: (%.2f, %.2f), Puddle center: (%.2f, %.2f)",
                    insideDistance, playerX, playerY, puddleCenterX, puddleCenterY)
            );
            
            // Test Case 4: Player just outside fall zone (distance > 12)
            float outsideDistance = FALL_ZONE_RADIUS + 0.1f + random.nextFloat() * 50.0f; // 12.1 to 62.1
            angle = random.nextFloat() * 2.0f * (float) Math.PI;
            playerX = puddleCenterX + outsideDistance * (float) Math.cos(angle);
            playerY = puddleCenterY + outsideDistance * (float) Math.sin(angle);
            
            result = system.checkCollision(playerX, playerY, puddles);
            assertFalse(
                result.hasCollision(),
                String.format("Collision should NOT occur when player is outside fall zone (distance = %.2f). " +
                    "Player: (%.2f, %.2f), Puddle center: (%.2f, %.2f)",
                    outsideDistance, playerX, playerY, puddleCenterX, puddleCenterY)
            );
            assertNull(result.getPuddle(), "Result should not contain a puddle when no collision");
        }
    }
    
    /**
     * Property: Collision detection with multiple puddles
     * For any configuration of multiple puddles, collision detection should 
     * correctly identify the first colliding puddle.
     * 
     * This property-based test runs 100 trials with random puddle configurations.
     */
    @Test
    public void collisionDetectionWithMultiplePuddles() {
        Random random = new Random(42); // Fixed seed for reproducibility
        
        // Run 100 trials
        for (int trial = 0; trial < 100; trial++) {
            PuddleCollisionSystem system = new PuddleCollisionSystem();
            
            // Generate random number of puddles (2-8)
            int puddleCount = 2 + random.nextInt(7);
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
            
            // Test with player far from all puddles
            float playerX = -100.0f;
            float playerY = -100.0f;
            
            PuddleCollisionResult result = system.checkCollision(playerX, playerY, puddles);
            assertFalse(
                result.hasCollision(),
                "No collision should occur when player is far from all puddles"
            );
            
            // Test with player near first puddle
            WaterPuddle firstPuddle = puddles.get(0);
            float centerX = firstPuddle.getX() + firstPuddle.getWidth() / 2.0f;
            float centerY = firstPuddle.getY() + firstPuddle.getHeight() / 2.0f;
            
            result = system.checkCollision(centerX, centerY, puddles);
            assertTrue(
                result.hasCollision(),
                "Collision should occur when player is at a puddle center"
            );
            assertNotNull(result.getPuddle(), "Result should contain the colliding puddle");
        }
    }
    
    /**
     * Property: Empty puddle list returns no collision
     * For any player position, an empty puddle list should return no collision.
     * 
     * This property-based test runs 100 trials with random player positions.
     */
    @Test
    public void emptyPuddleListReturnsNoCollision() {
        Random random = new Random(42); // Fixed seed for reproducibility
        
        // Run 100 trials
        for (int trial = 0; trial < 100; trial++) {
            PuddleCollisionSystem system = new PuddleCollisionSystem();
            
            // Generate random player position
            float playerX = random.nextFloat() * 1000.0f;
            float playerY = random.nextFloat() * 1000.0f;
            
            // Test with empty list
            List<WaterPuddle> emptyList = new ArrayList<>();
            PuddleCollisionResult result = system.checkCollision(playerX, playerY, emptyList);
            
            assertFalse(
                result.hasCollision(),
                "No collision should occur with empty puddle list"
            );
            assertNull(result.getPuddle(), "Result should not contain a puddle");
            
            // Test with null list
            result = system.checkCollision(playerX, playerY, null);
            
            assertFalse(
                result.hasCollision(),
                "No collision should occur with null puddle list"
            );
            assertNull(result.getPuddle(), "Result should not contain a puddle");
        }
    }
    
    /**
     * Property: Invalid player positions are handled gracefully
     * For any invalid player position (NaN, Infinity), the system should 
     * handle it gracefully without crashing.
     * 
     * This property-based test runs 100 trials with various invalid positions.
     */
    @Test
    public void invalidPlayerPositionsHandledGracefully() {
        Random random = new Random(42); // Fixed seed for reproducibility
        
        // Run 100 trials
        for (int trial = 0; trial < 100; trial++) {
            PuddleCollisionSystem system = new PuddleCollisionSystem();
            
            // Create a valid puddle
            WaterPuddle puddle = new WaterPuddle();
            puddle.reset(100.0f, 100.0f, 30.0f, 25.0f, 0.0f);
            
            List<WaterPuddle> puddles = new ArrayList<>();
            puddles.add(puddle);
            
            // Test with NaN positions
            PuddleCollisionResult result = system.checkCollision(Float.NaN, 100.0f, puddles);
            assertFalse(
                result.hasCollision(),
                "No collision should occur with NaN X position"
            );
            
            result = system.checkCollision(100.0f, Float.NaN, puddles);
            assertFalse(
                result.hasCollision(),
                "No collision should occur with NaN Y position"
            );
            
            // Test with Infinity positions
            result = system.checkCollision(Float.POSITIVE_INFINITY, 100.0f, puddles);
            assertFalse(
                result.hasCollision(),
                "No collision should occur with Infinity X position"
            );
            
            result = system.checkCollision(100.0f, Float.NEGATIVE_INFINITY, puddles);
            assertFalse(
                result.hasCollision(),
                "No collision should occur with Infinity Y position"
            );
        }
    }
}
