package wagemaker.uk.weather;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for fall animation state query methods.
 * Validates: Requirements 1.3, 1.4
 */
public class FallAnimationStateQueryTest {
    
    /**
     * Test isFallSequenceActive() method.
     */
    @Test
    public void testIsFallSequenceActive() {
        FallAnimationSystem system = new FallAnimationSystem();
        
        // Initially should not be active (NONE state)
        assertFalse(system.isFallSequenceActive(), 
                   "Fall sequence should not be active initially");
        
        // Start sequence - should be active
        system.startFallSequence();
        assertTrue(system.isFallSequenceActive(), 
                  "Fall sequence should be active after starting");
        
        // Advance through states - should remain active
        for (int i = 0; i < 4; i++) {
            system.update(0.8f);
            assertTrue(system.isFallSequenceActive(), 
                      "Fall sequence should remain active during animation");
        }
        
        // Complete the sequence - should not be active
        system.update(0.8f);
        assertFalse(system.isFallSequenceActive(), 
                   "Fall sequence should not be active after completion");
    }
    
    /**
     * Test isFallSequenceComplete() method.
     */
    @Test
    public void testIsFallSequenceComplete() {
        FallAnimationSystem system = new FallAnimationSystem();
        
        // Initially should not be complete
        assertFalse(system.isFallSequenceComplete(), 
                   "Fall sequence should not be complete initially");
        
        // Start sequence - should not be complete
        system.startFallSequence();
        assertFalse(system.isFallSequenceComplete(), 
                   "Fall sequence should not be complete after starting");
        
        // Advance through states - should not be complete
        for (int i = 0; i < 4; i++) {
            system.update(0.8f);
            assertFalse(system.isFallSequenceComplete(), 
                       "Fall sequence should not be complete during animation");
        }
        
        // Complete the sequence - should be complete
        system.update(0.8f);
        assertTrue(system.isFallSequenceComplete(), 
                  "Fall sequence should be complete after final frame");
    }
    
    /**
     * Test reset() method.
     */
    @Test
    public void testReset() {
        FallAnimationSystem system = new FallAnimationSystem();
        
        // Start and advance sequence
        system.startFallSequence();
        system.update(1.6f); // Advance 2 frames
        
        assertTrue(system.isFallSequenceActive(), 
                  "Fall sequence should be active before reset");
        
        // Reset
        system.reset();
        
        // Verify state is back to NONE
        assertFalse(system.isFallSequenceActive(), 
                   "Fall sequence should not be active after reset");
        assertFalse(system.isFallSequenceComplete(), 
                   "Fall sequence should not be complete after reset");
        assertEquals(FallAnimationSystem.FallAnimationState.NONE, 
                    system.getCurrentState(),
                    "State should be NONE after reset");
    }
    
    /**
     * Test reset() after completion.
     */
    @Test
    public void testResetAfterCompletion() {
        FallAnimationSystem system = new FallAnimationSystem();
        
        // Complete the sequence
        system.startFallSequence();
        for (int i = 0; i < 5; i++) {
            system.update(0.8f);
        }
        
        assertTrue(system.isFallSequenceComplete(), 
                  "Fall sequence should be complete");
        
        // Reset
        system.reset();
        
        // Verify state is back to NONE
        assertFalse(system.isFallSequenceActive(), 
                   "Fall sequence should not be active after reset");
        assertFalse(system.isFallSequenceComplete(), 
                   "Fall sequence should not be complete after reset");
        
        // Should be able to start a new sequence
        system.startFallSequence();
        assertTrue(system.isFallSequenceActive(), 
                  "Should be able to start new sequence after reset");
    }
    
    /**
     * Test state query methods consistency.
     */
    @Test
    public void testStateQueryConsistency() {
        FallAnimationSystem system = new FallAnimationSystem();
        
        // NONE state: not active, not complete
        assertFalse(system.isFallSequenceActive());
        assertFalse(system.isFallSequenceComplete());
        
        // Active states: active, not complete
        system.startFallSequence();
        assertTrue(system.isFallSequenceActive());
        assertFalse(system.isFallSequenceComplete());
        
        // COMPLETE state: not active, complete
        for (int i = 0; i < 5; i++) {
            system.update(0.8f);
        }
        assertFalse(system.isFallSequenceActive());
        assertTrue(system.isFallSequenceComplete());
    }
}
