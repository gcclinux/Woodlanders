package wagemaker.uk.weather;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * Performance tests for fall animation system.
 * Validates that animation updates and frame extraction meet performance targets.
 */
public class AnimationSystemPerformanceTest {
    
    private static final int MULTIPLE_PLAYER_COUNT = 10;
    private static final float TARGET_TIME_MS = 0.1f; // Very low target for animation updates
    private static final int WARMUP_ITERATIONS = 100;
    private static final int TEST_ITERATIONS = 1000;
    private static HeadlessApplication application;
    
    @BeforeAll
    public static void setupGdx() {
        // Mock GL20 for headless testing
        Gdx.gl = mock(GL20.class);
        Gdx.gl20 = mock(GL20.class);
        
        // Create headless application for LibGDX context
        HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
        application = new HeadlessApplication(new ApplicationAdapter() {}, config);
    }
    
    @AfterAll
    public static void teardownGdx() {
        if (application != null) {
            application.exit();
            application = null;
        }
    }
    
    /**
     * Test animation update performance with single player.
     * Validates that animation state updates complete quickly.
     * 
     * Requirements: Performance Testing
     */
    @Test
    public void animationUpdatePerformanceSinglePlayer() {
        FallAnimationSystem system = new FallAnimationSystem();
        system.startFallSequence();
        
        float deltaTime = 0.016f; // ~60 FPS
        
        // Warmup
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            system.update(deltaTime);
        }
        
        // Reset for actual test
        system.reset();
        system.startFallSequence();
        
        // Measure update performance
        long totalNanos = 0;
        int updateCount = 0;
        
        while (system.isFallSequenceActive() && updateCount < TEST_ITERATIONS) {
            long startTime = System.nanoTime();
            system.update(deltaTime);
            long endTime = System.nanoTime();
            
            totalNanos += (endTime - startTime);
            updateCount++;
        }
        
        double averageMillis = (double) totalNanos / updateCount / 1_000_000.0;
        
        System.out.printf("Animation update performance (single player):%n");
        System.out.printf("  Update count: %d%n", updateCount);
        System.out.printf("  Average time per update: %.6f ms%n", averageMillis);
        System.out.printf("  Target time: %.1f ms%n", TARGET_TIME_MS);
        
        assertTrue(
            averageMillis < TARGET_TIME_MS,
            String.format("Animation update took %.6f ms, exceeds target of %.1f ms",
                averageMillis, TARGET_TIME_MS)
        );
    }
    
    /**
     * Test animation update performance with multiple players.
     * Simulates multiple players falling simultaneously.
     * 
     * Requirements: Performance Testing
     */
    @Test
    public void animationUpdatePerformanceMultiplePlayers() {
        List<FallAnimationSystem> systems = new ArrayList<>();
        
        // Create multiple animation systems (simulating multiple players)
        for (int i = 0; i < MULTIPLE_PLAYER_COUNT; i++) {
            FallAnimationSystem system = new FallAnimationSystem();
            system.startFallSequence();
            systems.add(system);
        }
        
        float deltaTime = 0.016f; // ~60 FPS
        
        // Warmup
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            for (FallAnimationSystem system : systems) {
                system.update(deltaTime);
            }
        }
        
        // Reset for actual test
        systems.clear();
        for (int i = 0; i < MULTIPLE_PLAYER_COUNT; i++) {
            FallAnimationSystem system = new FallAnimationSystem();
            system.startFallSequence();
            systems.add(system);
        }
        
        // Measure update performance for all players
        long totalNanos = 0;
        int frameCount = 0;
        
        // Run for a fixed number of frames
        for (int frame = 0; frame < TEST_ITERATIONS; frame++) {
            long frameStartTime = System.nanoTime();
            
            // Update all player animations
            for (FallAnimationSystem system : systems) {
                if (system.isFallSequenceActive()) {
                    system.update(deltaTime);
                }
            }
            
            long frameEndTime = System.nanoTime();
            totalNanos += (frameEndTime - frameStartTime);
            frameCount++;
        }
        
        double averageMillis = (double) totalNanos / frameCount / 1_000_000.0;
        double perPlayerMillis = averageMillis / MULTIPLE_PLAYER_COUNT;
        
        System.out.printf("Animation update performance (multiple players):%n");
        System.out.printf("  Player count: %d%n", MULTIPLE_PLAYER_COUNT);
        System.out.printf("  Frame count: %d%n", frameCount);
        System.out.printf("  Average time per frame: %.6f ms%n", averageMillis);
        System.out.printf("  Average time per player: %.6f ms%n", perPlayerMillis);
        System.out.printf("  Target time per player: %.1f ms%n", TARGET_TIME_MS);
        
        assertTrue(
            perPlayerMillis < TARGET_TIME_MS,
            String.format("Animation update per player took %.6f ms, exceeds target of %.1f ms",
                perPlayerMillis, TARGET_TIME_MS)
        );
    }
    
    /**
     * Test animation state transition performance.
     * Validates that state transitions happen efficiently.
     * 
     * Requirements: Performance Testing
     */
    @Test
    public void animationStateTransitionPerformance() {
        FallAnimationSystem system = new FallAnimationSystem();
        
        // Warmup
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            system.startFallSequence();
            system.reset();
        }
        
        // Measure state transition performance
        long totalNanos = 0;
        
        for (int i = 0; i < TEST_ITERATIONS; i++) {
            long startTime = System.nanoTime();
            system.startFallSequence();
            long endTime = System.nanoTime();
            
            totalNanos += (endTime - startTime);
            
            // Verify state changed
            assertTrue(system.isFallSequenceActive(), "Sequence should be active after start");
            
            system.reset();
        }
        
        double averageMillis = (double) totalNanos / TEST_ITERATIONS / 1_000_000.0;
        
        System.out.printf("Animation state transition performance:%n");
        System.out.printf("  Transition count: %d%n", TEST_ITERATIONS);
        System.out.printf("  Average time per transition: %.6f ms%n", averageMillis);
        System.out.printf("  Target time: %.1f ms%n", TARGET_TIME_MS);
        
        assertTrue(
            averageMillis < TARGET_TIME_MS,
            String.format("State transition took %.6f ms, exceeds target of %.1f ms",
                averageMillis, TARGET_TIME_MS)
        );
    }
    
    /**
     * Test complete animation sequence performance.
     * Measures time to complete entire fall-standup sequence.
     * 
     * Requirements: Performance Testing
     */
    @Test
    public void completeAnimationSequencePerformance() {
        float deltaTime = 0.016f; // ~60 FPS
        
        // Warmup
        for (int i = 0; i < 10; i++) {
            FallAnimationSystem system = new FallAnimationSystem();
            system.startFallSequence();
            
            while (system.isFallSequenceActive()) {
                system.update(deltaTime);
            }
        }
        
        // Measure complete sequence performance
        long totalNanos = 0;
        int sequenceCount = 100; // Run fewer sequences since each takes ~4 seconds
        
        for (int i = 0; i < sequenceCount; i++) {
            FallAnimationSystem system = new FallAnimationSystem();
            system.startFallSequence();
            
            long startTime = System.nanoTime();
            
            while (system.isFallSequenceActive()) {
                system.update(deltaTime);
            }
            
            long endTime = System.nanoTime();
            totalNanos += (endTime - startTime);
            
            assertTrue(system.isFallSequenceComplete(), "Sequence should be complete");
        }
        
        double averageMillis = (double) totalNanos / sequenceCount / 1_000_000.0;
        
        System.out.printf("Complete animation sequence performance:%n");
        System.out.printf("  Sequence count: %d%n", sequenceCount);
        System.out.printf("  Average time per sequence: %.4f ms%n", averageMillis);
        
        // Verify sequence completes in reasonable time (should be ~4 seconds of game time)
        // But actual processing should be very fast
        assertTrue(
            averageMillis < 10.0, // 10ms is very generous for processing
            String.format("Complete sequence processing took %.4f ms, too slow", averageMillis)
        );
    }
    
    /**
     * Test animation system memory efficiency.
     * Validates that animation system doesn't allocate excessive memory.
     * 
     * Requirements: Performance Testing
     */
    @Test
    public void animationSystemMemoryEfficiency() {
        // Force garbage collection before test
        System.gc();
        Thread.yield();
        
        Runtime runtime = Runtime.getRuntime();
        long memoryBefore = runtime.totalMemory() - runtime.freeMemory();
        
        // Create many animation systems
        List<FallAnimationSystem> systems = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            FallAnimationSystem system = new FallAnimationSystem();
            system.startFallSequence();
            systems.add(system);
        }
        
        // Update them all
        float deltaTime = 0.016f;
        for (int frame = 0; frame < 100; frame++) {
            for (FallAnimationSystem system : systems) {
                if (system.isFallSequenceActive()) {
                    system.update(deltaTime);
                }
            }
        }
        
        long memoryAfter = runtime.totalMemory() - runtime.freeMemory();
        long memoryUsed = memoryAfter - memoryBefore;
        double memoryPerSystem = (double) memoryUsed / 1000.0 / 1024.0; // KB per system
        
        System.out.printf("Animation system memory efficiency:%n");
        System.out.printf("  Systems created: 1000%n");
        System.out.printf("  Memory used: %.2f KB%n", memoryUsed / 1024.0);
        System.out.printf("  Memory per system: %.2f KB%n", memoryPerSystem);
        
        // Verify reasonable memory usage (< 2KB per system is very reasonable)
        assertTrue(
            memoryPerSystem < 2.0,
            String.format("Animation system uses %.2f KB per instance, too much memory", 
                memoryPerSystem)
        );
        
        // Clean up
        systems.clear();
    }
}
