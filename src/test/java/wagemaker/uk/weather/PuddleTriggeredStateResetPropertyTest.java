package wagemaker.uk.weather;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for puddle triggered state reset on exit.
 * Feature: puddle-fall-damage, Property 10: Triggered state reset on exit
 * Validates: Requirements 4.3
 */
public class PuddleTriggeredStateResetPropertyTest {
    
    private static final float FALL_ZONE_RADIUS = 12.0f;
    
    /**
     * Property 10: Triggered state reset on exit
     * For any triggered puddle, when the player moves outside its fall zone 
     * (distance > 12 pixels), the triggered state should be reset.
     * 
     * Validates: Requirements 4.3
     * 
     * This property-based test runs 100 trials with random positions.
     */
    @Test
    public void triggeredStateResetsOnExit() {
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
            
            // Step 1: Trigger collision at puddle center
            PuddleCollisionResult result = system.checkCollision(puddleCenterX, puddleCenterY, puddles);
            assertTrue(
                result.hasCollision(),
                "Initial collision should occur at puddle center"
            );
            
            // Mark puddle as triggered
            system.markPuddleTriggered(puddle);
            
            // Step 2: Verify puddle is triggered (no collision at center)
            result = system.checkCollision(puddleCenterX, puddleCenterY, puddles);
            assertFalse(
                result.hasCollision(),
                "Puddle should be triggered, no collision at center"
            );
            
            // Step 3: Move player outside fall zone
            float angle = random.nextFloat() * 2.0f * (float) Math.PI;
            float outsideDistance = FALL_ZONE_RADIUS + 1.0f + random.nextFloat() * 50.0f; // 13+ pixels
            float playerX = puddleCenterX + outsideDistance * (float) Math.cos(angle);
            float playerY = puddleCenterY + outsideDistance * (float) Math.sin(angle);
            
            // Update triggered states (player is now outside)
            system.updateTriggeredStates(playerX, playerY, puddles);
            
            // Step 4: Move player back to puddle center
            // Triggered state should be reset, so collision should occur again
            result = system.checkCollision(puddleCenterX, puddleCenterY, puddles);
            assertTrue(
                result.hasCollision(),
                String.format("Collision should occur again after exiting and re-entering. " +
                    "Puddle center: (%.2f, %.2f), Exit position: (%.2f, %.2f), Distance: %.2f",
                    puddleCenterX, puddleCenterY, playerX, playerY, outsideDistance)
            );
            assertEquals(puddle, result.getPuddle(), "Result should contain the puddle");
        }
    }
    
    /**
     * Property: Triggered state does not reset while player remains in zone
     * For any triggered puddle, as long as the player remains within the fall zone,
     * the triggered state should persist.
     * 
     * This property-based test runs 100 trials with random movements within zone.
     */
    @Test
    public void triggeredStateDoesNotResetWhileInZone() {
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
            
            // Move player around within fall zone multiple times
            int numMoves = 10 + random.nextInt(20); // 10-29 moves
            
            for (int i = 0; i < numMoves; i++) {
                // Random position within fall zone
                float angle = random.nextFloat() * 2.0f * (float) Math.PI;
                float distance = random.nextFloat() * (FALL_ZONE_RADIUS - 0.5f); // Stay safely inside
                float playerX = puddleCenterX + distance * (float) Math.cos(angle);
                float playerY = puddleCenterY + distance * (float) Math.sin(angle);
                
                // Update triggered states
                system.updateTriggeredStates(playerX, playerY, puddles);
                
                // Check collision - should still be triggered (no collision)
                PuddleCollisionResult result = system.checkCollision(playerX, playerY, puddles);
                assertFalse(
                    result.hasCollision(),
                    String.format("Triggered state should persist while in zone (move %d/%d). " +
                        "Player: (%.2f, %.2f), Puddle center: (%.2f, %.2f), Distance: %.2f",
                        i + 1, numMoves, playerX, playerY, puddleCenterX, puddleCenterY, distance)
                );
            }
        }
    }
    
    /**
     * Property: Exit and re-enter cycle
     * For any triggered puddle, the player should be able to exit and re-enter 
     * multiple times, with the triggered state resetting each time they exit.
     * 
     * This property-based test runs 100 trials with multiple exit/re-enter cycles.
     */
    @Test
    public void exitAndReenterCycle() {
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
            
            // Perform multiple exit/re-enter cycles
            int numCycles = 3 + random.nextInt(5); // 3-7 cycles
            
            for (int cycle = 0; cycle < numCycles; cycle++) {
                // Enter puddle and trigger
                PuddleCollisionResult result = system.checkCollision(puddleCenterX, puddleCenterY, puddles);
                assertTrue(
                    result.hasCollision(),
                    String.format("Collision should occur on cycle %d entry", cycle + 1)
                );
                
                system.markPuddleTriggered(puddle);
                
                // Verify triggered
                result = system.checkCollision(puddleCenterX, puddleCenterY, puddles);
                assertFalse(
                    result.hasCollision(),
                    String.format("Puddle should be triggered on cycle %d", cycle + 1)
                );
                
                // Exit puddle
                float angle = random.nextFloat() * 2.0f * (float) Math.PI;
                float outsideDistance = FALL_ZONE_RADIUS + 5.0f + random.nextFloat() * 20.0f;
                float playerX = puddleCenterX + outsideDistance * (float) Math.cos(angle);
                float playerY = puddleCenterY + outsideDistance * (float) Math.sin(angle);
                
                system.updateTriggeredStates(playerX, playerY, puddles);
                
                // Verify reset by checking collision at center again
                // (This will be verified in the next cycle iteration)
            }
        }
    }
    
    /**
     * Property: Partial exit does not reset
     * For any triggered puddle, moving to the edge of the fall zone but not 
     * exiting should not reset the triggered state.
     * 
     * This property-based test runs 100 trials with edge movements.
     */
    @Test
    public void partialExitDoesNotReset() {
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
            
            // Move to edge of fall zone (just inside)
            float angle = random.nextFloat() * 2.0f * (float) Math.PI;
            float edgeDistance = FALL_ZONE_RADIUS - 0.5f; // Just inside boundary
            float playerX = puddleCenterX + edgeDistance * (float) Math.cos(angle);
            float playerY = puddleCenterY + edgeDistance * (float) Math.sin(angle);
            
            // Update triggered states
            system.updateTriggeredStates(playerX, playerY, puddles);
            
            // Check collision - should still be triggered (no collision)
            PuddleCollisionResult result = system.checkCollision(playerX, playerY, puddles);
            assertFalse(
                result.hasCollision(),
                String.format("Triggered state should not reset at edge. " +
                    "Player: (%.2f, %.2f), Puddle center: (%.2f, %.2f), Distance: %.2f",
                    playerX, playerY, puddleCenterX, puddleCenterY, edgeDistance)
            );
        }
    }
}
