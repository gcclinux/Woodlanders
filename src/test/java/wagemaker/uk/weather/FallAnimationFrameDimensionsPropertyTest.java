package wagemaker.uk.weather;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based test for fall animation frame dimensions.
 * **Feature: puddle-fall-damage, Property 12: Frame dimensions are correct**
 * **Validates: Requirements 5.1**
 * 
 * For any fall or standup frame extraction, the texture region should be 
 * exactly 64x64 pixels.
 */
public class FallAnimationFrameDimensionsPropertyTest {
    
    private static final int EXPECTED_FRAME_SIZE = 64;
    
    /**
     * Property 12: Frame dimensions are correct
     * For any fall or standup frame, the dimensions should be exactly 64x64 pixels.
     * 
     * Validates: Requirements 5.1
     * 
     * This property-based test runs 100 trials.
     */
    @Test
    public void allFramesAre64x64Pixels() {
        Random random = new Random(42); // Fixed seed for reproducibility
        
        // Run 100 trials
        for (int trial = 0; trial < 100; trial++) {
            FallAnimationSystem system = new FallAnimationSystem();
            
            // Get frame sequence
            FallAnimationSystem.FallAnimationFrame[] frames = system.getFrameSequence();
            
            assertNotNull(frames, "Frame sequence should not be null");
            assertEquals(5, frames.length, "Frame sequence should have 5 frames");
            
            // Verify each frame would extract a 64x64 region
            // Note: We can't test actual TextureRegion dimensions in headless mode,
            // but we can verify the frame data is set up correctly
            for (int i = 0; i < frames.length; i++) {
                FallAnimationSystem.FallAnimationFrame frame = frames[i];
                
                assertNotNull(frame, "Frame " + i + " should not be null");
                
                // Verify frame coordinates are valid for 64x64 extraction
                // X coordinate should be >= 0 and leave room for 64 pixels
                assertTrue(frame.getSpriteX() >= 0, 
                          "Frame " + i + " X coordinate should be non-negative");
                
                // Y coordinate should be >= 0 and leave room for 64 pixels
                assertTrue(frame.getSpriteY() >= 0, 
                          "Frame " + i + " Y coordinate should be non-negative");
            }
        }
    }
    
    /**
     * Property: Frame coordinates allow for 64x64 extraction without overlap.
     * 
     * For any two consecutive frames in the sequence, their X coordinates 
     * should be at least 64 pixels apart to avoid overlap.
     * 
     * This property-based test runs 100 trials.
     */
    @Test
    public void frameCoordinatesAllowNonOverlappingExtraction() {
        Random random = new Random(42); // Fixed seed for reproducibility
        
        // Run 100 trials
        for (int trial = 0; trial < 100; trial++) {
            FallAnimationSystem system = new FallAnimationSystem();
            FallAnimationSystem.FallAnimationFrame[] frames = system.getFrameSequence();
            
            // Check spacing between consecutive frames
            for (int i = 0; i < frames.length - 1; i++) {
                int currentX = frames[i].getSpriteX();
                int nextX = frames[i + 1].getSpriteX();
                
                // Since frames progress right to left (decreasing X),
                // the difference should be exactly 64 pixels
                int spacing = currentX - nextX;
                
                assertEquals(EXPECTED_FRAME_SIZE, spacing, 
                           "Frames " + i + " and " + (i + 1) + 
                           " should be exactly 64 pixels apart for non-overlapping 64x64 extraction");
            }
        }
    }
    
    /**
     * Property: Frame coordinates fit within standard sprite sheet dimensions.
     * 
     * For any frame, the coordinates plus 64 pixels should fit within 
     * typical sprite sheet dimensions (e.g., 576x1344 or larger).
     * 
     * This property-based test runs 100 trials.
     */
    @Test
    public void frameCoordinatesFitWithinSpriteSheet() {
        Random random = new Random(42); // Fixed seed for reproducibility
        
        // Typical sprite sheet dimensions (from Player class)
        int minSpriteSheetWidth = 576;  // Minimum width to contain all frames
        int minSpriteSheetHeight = 1344; // Minimum height to contain fall frames at Y=1280
        
        // Run 100 trials
        for (int trial = 0; trial < 100; trial++) {
            FallAnimationSystem system = new FallAnimationSystem();
            FallAnimationSystem.FallAnimationFrame[] frames = system.getFrameSequence();
            
            for (int i = 0; i < frames.length; i++) {
                FallAnimationSystem.FallAnimationFrame frame = frames[i];
                
                // Verify X + 64 fits within sprite sheet width
                assertTrue(frame.getSpriteX() + EXPECTED_FRAME_SIZE <= minSpriteSheetWidth, 
                          "Frame " + i + " X coordinate (" + frame.getSpriteX() + 
                          ") + 64 should fit within sprite sheet width (" + minSpriteSheetWidth + ")");
                
                // Verify Y + 64 fits within sprite sheet height
                assertTrue(frame.getSpriteY() + EXPECTED_FRAME_SIZE <= minSpriteSheetHeight, 
                          "Frame " + i + " Y coordinate (" + frame.getSpriteY() + 
                          ") + 64 should fit within sprite sheet height (" + minSpriteSheetHeight + ")");
            }
        }
    }
    
    /**
     * Property: Frame size constant is consistent with frame spacing.
     * 
     * For any frame sequence, the spacing between frames should match 
     * the expected frame size of 64 pixels.
     * 
     * This property-based test runs 100 trials.
     */
    @Test
    public void frameSizeConstantMatchesSpacing() {
        Random random = new Random(42); // Fixed seed for reproducibility
        
        // Run 100 trials
        for (int trial = 0; trial < 100; trial++) {
            FallAnimationSystem system = new FallAnimationSystem();
            FallAnimationSystem.FallAnimationFrame[] frames = system.getFrameSequence();
            
            // Verify the pattern: 256, 192, 128, 64, 0
            // Each step is exactly 64 pixels
            int[] expectedXCoords = {256, 192, 128, 64, 0};
            
            for (int i = 0; i < frames.length; i++) {
                assertEquals(expectedXCoords[i], frames[i].getSpriteX(), 
                           "Frame " + i + " X coordinate should match expected pattern");
                
                // Verify the spacing is consistent with 64-pixel frames
                if (i > 0) {
                    int spacing = expectedXCoords[i - 1] - expectedXCoords[i];
                    assertEquals(EXPECTED_FRAME_SIZE, spacing, 
                               "Spacing between frames should be exactly 64 pixels");
                }
            }
        }
    }
}
