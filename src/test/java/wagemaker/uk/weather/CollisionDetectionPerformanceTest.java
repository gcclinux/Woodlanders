package wagemaker.uk.weather;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Performance tests for collision detection system.
 * Validates that collision detection meets performance targets.
 */
public class CollisionDetectionPerformanceTest {
    
    private static final int MAX_PUDDLE_COUNT = 8;
    private static final float TARGET_TIME_MS = 1.0f;
    private static final int WARMUP_ITERATIONS = 100;
    private static final int TEST_ITERATIONS = 1000;
    
    /**
     * Test collision detection performance with maximum puddle count.
     * Validates that collision checks complete in < 1ms per frame.
     * 
     * Requirements: Performance Testing
     */
    @Test
    public void collisionDetectionPerformanceWithMaxPuddles() {
        Random random = new Random(42);
        PuddleCollisionSystem system = new PuddleCollisionSystem();
        
        // Create maximum number of puddles
        List<WaterPuddle> puddles = new ArrayList<>();
        for (int i = 0; i < MAX_PUDDLE_COUNT; i++) {
            float puddleX = random.nextFloat() * 1000.0f;
            float puddleY = random.nextFloat() * 1000.0f;
            float puddleWidth = 20.0f + random.nextFloat() * 30.0f;
            float puddleHeight = 15.0f + random.nextFloat() * 25.0f;
            
            WaterPuddle puddle = new WaterPuddle();
            puddle.reset(puddleX, puddleY, puddleWidth, puddleHeight, 0.0f);
            puddles.add(puddle);
        }
        
        // Warmup phase - JIT compilation
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            float playerX = random.nextFloat() * 1000.0f;
            float playerY = random.nextFloat() * 1000.0f;
            system.checkCollision(playerX, playerY, puddles);
        }
        
        // Measure performance over many iterations
        long totalNanos = 0;
        for (int i = 0; i < TEST_ITERATIONS; i++) {
            float playerX = random.nextFloat() * 1000.0f;
            float playerY = random.nextFloat() * 1000.0f;
            
            long startTime = System.nanoTime();
            PuddleCollisionResult result = system.checkCollision(playerX, playerY, puddles);
            long endTime = System.nanoTime();
            
            totalNanos += (endTime - startTime);
            
            // Verify result is valid
            assertNotNull(result, "Collision result should not be null");
        }
        
        // Calculate average time per check
        double averageNanos = (double) totalNanos / TEST_ITERATIONS;
        double averageMillis = averageNanos / 1_000_000.0;
        
        System.out.printf("Collision detection performance:%n");
        System.out.printf("  Puddle count: %d%n", MAX_PUDDLE_COUNT);
        System.out.printf("  Test iterations: %d%n", TEST_ITERATIONS);
        System.out.printf("  Average time per check: %.4f ms%n", averageMillis);
        System.out.printf("  Target time: %.1f ms%n", TARGET_TIME_MS);
        
        // Verify performance meets target
        assertTrue(
            averageMillis < TARGET_TIME_MS,
            String.format("Collision detection took %.4f ms per frame, exceeds target of %.1f ms",
                averageMillis, TARGET_TIME_MS)
        );
    }
    
    /**
     * Test collision detection performance with varying puddle counts.
     * Validates that performance scales linearly with puddle count.
     * 
     * Requirements: Performance Testing
     */
    @Test
    public void collisionDetectionScalesLinearly() {
        Random random = new Random(42);
        
        int[] puddleCounts = {1, 2, 4, 8};
        double[] averageTimes = new double[puddleCounts.length];
        
        for (int countIndex = 0; countIndex < puddleCounts.length; countIndex++) {
            int puddleCount = puddleCounts[countIndex];
            PuddleCollisionSystem system = new PuddleCollisionSystem();
            
            // Create puddles
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
            
            // Warmup
            for (int i = 0; i < WARMUP_ITERATIONS; i++) {
                float playerX = random.nextFloat() * 1000.0f;
                float playerY = random.nextFloat() * 1000.0f;
                system.checkCollision(playerX, playerY, puddles);
            }
            
            // Measure
            long totalNanos = 0;
            for (int i = 0; i < TEST_ITERATIONS; i++) {
                float playerX = random.nextFloat() * 1000.0f;
                float playerY = random.nextFloat() * 1000.0f;
                
                long startTime = System.nanoTime();
                system.checkCollision(playerX, playerY, puddles);
                long endTime = System.nanoTime();
                
                totalNanos += (endTime - startTime);
            }
            
            averageTimes[countIndex] = (double) totalNanos / TEST_ITERATIONS / 1_000_000.0;
        }
        
        // Print results
        System.out.printf("Collision detection scaling:%n");
        for (int i = 0; i < puddleCounts.length; i++) {
            System.out.printf("  %d puddles: %.4f ms%n", puddleCounts[i], averageTimes[i]);
        }
        
        // Verify all times are under target
        for (int i = 0; i < puddleCounts.length; i++) {
            assertTrue(
                averageTimes[i] < TARGET_TIME_MS,
                String.format("Collision detection with %d puddles took %.4f ms, exceeds target of %.1f ms",
                    puddleCounts[i], averageTimes[i], TARGET_TIME_MS)
            );
        }
        
        // Verify roughly linear scaling (8 puddles should take < 8x time of 1 puddle)
        // Allow some overhead, so use 10x as threshold
        double scalingFactor = averageTimes[3] / averageTimes[0];
        assertTrue(
            scalingFactor < 10.0,
            String.format("Collision detection scaling factor %.2f suggests non-linear performance", 
                scalingFactor)
        );
    }
    
    /**
     * Test collision detection performance with worst-case scenario.
     * Player positioned to check all puddles before finding collision.
     * 
     * Requirements: Performance Testing
     */
    @Test
    public void collisionDetectionWorstCasePerformance() {
        Random random = new Random(42);
        PuddleCollisionSystem system = new PuddleCollisionSystem();
        
        // Create puddles far apart
        List<WaterPuddle> puddles = new ArrayList<>();
        for (int i = 0; i < MAX_PUDDLE_COUNT; i++) {
            float puddleX = i * 200.0f; // Space them out
            float puddleY = i * 200.0f;
            float puddleWidth = 30.0f;
            float puddleHeight = 25.0f;
            
            WaterPuddle puddle = new WaterPuddle();
            puddle.reset(puddleX, puddleY, puddleWidth, puddleHeight, 0.0f);
            puddles.add(puddle);
        }
        
        // Warmup
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            system.checkCollision(5000.0f, 5000.0f, puddles);
        }
        
        // Measure worst case: player far from all puddles (checks all)
        long totalNanos = 0;
        for (int i = 0; i < TEST_ITERATIONS; i++) {
            long startTime = System.nanoTime();
            PuddleCollisionResult result = system.checkCollision(5000.0f, 5000.0f, puddles);
            long endTime = System.nanoTime();
            
            totalNanos += (endTime - startTime);
            
            assertFalse(result.hasCollision(), "Should not collide when far from all puddles");
        }
        
        double averageMillis = (double) totalNanos / TEST_ITERATIONS / 1_000_000.0;
        
        System.out.printf("Collision detection worst-case performance:%n");
        System.out.printf("  Average time: %.4f ms%n", averageMillis);
        System.out.printf("  Target time: %.1f ms%n", TARGET_TIME_MS);
        
        assertTrue(
            averageMillis < TARGET_TIME_MS,
            String.format("Worst-case collision detection took %.4f ms, exceeds target of %.1f ms",
                averageMillis, TARGET_TIME_MS)
        );
    }
}
