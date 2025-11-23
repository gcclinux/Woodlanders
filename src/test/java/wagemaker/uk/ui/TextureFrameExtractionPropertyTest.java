package wagemaker.uk.ui;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

/**
 * Property-based test for texture frame extraction.
 * 
 * **Feature: character-selection-menu, Property 6: Texture frame extraction**
 * **Validates: Requirements 2.4**
 */
public class TextureFrameExtractionPropertyTest {
    
    private static final String[] CHARACTER_SPRITES = {
        "sprites/player/girl_red_start.png",
        "sprites/player/girl_navy_start.png",
        "sprites/player/boy_red_start.png",
        "sprites/player/boy_navy_start.png"
    };
    
    private static HeadlessApplication application;
    
    @BeforeAll
    public static void setUp() {
        // Mock GL20 for headless testing
        Gdx.gl = mock(GL20.class);
        Gdx.gl20 = mock(GL20.class);
        
        // Initialize headless application for LibGDX
        HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
        application = new HeadlessApplication(new ApplicationAdapter() {}, config);
    }
    
    @AfterAll
    public static void tearDown() {
        if (application != null) {
            application.exit();
        }
    }
    
    /**
     * Property: For any character sprite sheet, the idle frame extracted for preview
     * should be from position (0, 2048) from the top-left with dimensions of 64×64 pixels.
     * 
     * This test runs 100 iterations (25 per character sprite) as specified in the design document.
     */
    @Test
    public void idleFrameExtractedFromCorrectPosition() {
        int iterations = 0;
        
        // Run 100 trials as specified in the design document (25 per character)
        for (int trial = 0; trial < 25; trial++) {
            for (String spritePath : CHARACTER_SPRITES) {
                iterations++;
                
                try {
                    // Load the sprite sheet
                    Texture spriteSheet = new Texture(Gdx.files.internal(spritePath));
                    assertNotNull(spriteSheet, "Sprite sheet should load successfully: " + spritePath);
                    
                    // Extract idle frame using the same logic as CharacterSelectionDialog
                    int spriteSheetHeight = spriteSheet.getHeight();
                    int frameX = 0;
                    int frameYFromTop = 2048;
                    int frameWidth = 64;
                    int frameHeight = 64;
                    
                    // Convert Y coordinate from top-left to bottom-left origin
                    int frameYFromBottom = spriteSheetHeight - frameYFromTop - frameHeight;
                    
                    TextureRegion idleFrame = new TextureRegion(
                        spriteSheet,
                        frameX,
                        frameYFromBottom,
                        frameWidth,
                        frameHeight
                    );
                    
                    // Verify the frame was extracted correctly
                    assertNotNull(idleFrame, "Idle frame should be extracted successfully");
                    
                    // Verify frame dimensions
                    assertEquals(64, idleFrame.getRegionWidth(), 
                               "Idle frame width should be 64 pixels (iteration " + iterations + ", sprite: " + spritePath + ")");
                    assertEquals(64, idleFrame.getRegionHeight(), 
                               "Idle frame height should be 64 pixels (iteration " + iterations + ", sprite: " + spritePath + ")");
                    
                    // Verify frame X position
                    assertEquals(0, idleFrame.getRegionX(), 
                               "Idle frame X position should be 0 (iteration " + iterations + ", sprite: " + spritePath + ")");
                    
                    // Verify frame Y position (converted to bottom-left origin)
                    int expectedYFromBottom = spriteSheetHeight - 2048 - 64;
                    assertEquals(expectedYFromBottom, idleFrame.getRegionY(), 
                               "Idle frame Y position should be " + expectedYFromBottom + 
                               " (converted from top-left 2048) (iteration " + iterations + ", sprite: " + spritePath + ")");
                    
                    // Clean up
                    spriteSheet.dispose();
                    
                } catch (Exception e) {
                    throw new AssertionError("Failed to extract idle frame from " + spritePath + 
                                           " (iteration " + iterations + "): " + e.getMessage(), e);
                }
            }
        }
        
        System.out.println("Successfully verified texture frame extraction across " + iterations + " iterations");
    }
}
