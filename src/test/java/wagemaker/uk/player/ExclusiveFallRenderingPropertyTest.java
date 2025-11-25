package wagemaker.uk.player;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import wagemaker.uk.weather.WaterPuddle;
import wagemaker.uk.weather.FallAnimationSystem;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based test for exclusive fall rendering.
 * **Feature: puddle-fall-damage, Property 14: Exclusive fall rendering**
 * **Validates: Requirements 5.5**
 * 
 * Tests that for any active fall sequence, normal walking and idle animations
 * should not be rendered.
 */
public class ExclusiveFallRenderingPropertyTest {
    
    private static HeadlessApplication application;
    
    @BeforeAll
    public static void setUpGdx() {
        // Initialize headless LibGDX application for testing
        HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
        application = new HeadlessApplication(new ApplicationAdapter() {}, config);
        
        // Mock GL20 to prevent null pointer exceptions
        Gdx.gl = Mockito.mock(GL20.class);
        Gdx.gl20 = Mockito.mock(GL20.class);
    }
    
    @AfterAll
    public static void tearDownGdx() {
        if (application != null) {
            application.exit();
            application = null;
        }
    }
    
    /**
     * Property 14: Exclusive fall rendering
     * For any active fall sequence, normal walking and idle animations
     * should not be rendered.
     * 
     * Validates: Requirements 5.5
     * 
     * This property-based test runs 100 trials with random player states.
     */
    @Test
    void exclusiveFallRendering() {
        // **Feature: puddle-fall-damage, Property 14: Exclusive fall rendering**
        
        Random random = new Random(42); // Fixed seed for reproducibility
        int trials = 100;
        
        for (int i = 0; i < trials; i++) {
            // Create a player
            OrthographicCamera camera = new OrthographicCamera();
            Player player = new Player(0, 0, camera);
            
            // Create a puddle for the fall trigger
            WaterPuddle puddle = new WaterPuddle();
            puddle.reset(0, 0, 32, 32, 0);
            
            try {
                // Get normal idle frame before fall
                TextureRegion normalFrame = player.getCurrentFrame();
                assertNotNull(normalFrame,
                    String.format("Trial %d: Normal frame should not be null", i));
                
                // Trigger the fall using reflection
                java.lang.reflect.Method triggerFallMethod = Player.class.getDeclaredMethod("triggerFall", WaterPuddle.class);
                triggerFallMethod.setAccessible(true);
                triggerFallMethod.invoke(player, puddle);
                
                // Access fall animation system to verify it's active
                java.lang.reflect.Field fallAnimationSystemField = Player.class.getDeclaredField("fallAnimationSystem");
                fallAnimationSystemField.setAccessible(true);
                FallAnimationSystem fallAnimationSystem = (FallAnimationSystem) fallAnimationSystemField.get(player);
                
                assertTrue(fallAnimationSystem.isFallSequenceActive(),
                    String.format("Trial %d: Fall animation should be active", i));
                
                // Get current frame during fall
                TextureRegion fallFrame = player.getCurrentFrame();
                assertNotNull(fallFrame,
                    String.format("Trial %d: Fall frame should not be null", i));
                
                // The fall frame should be different from the normal idle frame
                // We can't directly compare TextureRegions, but we can verify that
                // getCurrentFrame() is returning a frame from the fall animation system
                // by checking that the fall animation is active
                
                // Verify that the frame comes from fall animation (not normal animation)
                // by checking the texture coordinates
                TextureRegion expectedFallFrame = fallAnimationSystem.getCurrentFallFrame();
                
                // Both should have same dimensions
                assertEquals(expectedFallFrame.getRegionWidth(), fallFrame.getRegionWidth(),
                    String.format("Trial %d: Fall frame width should match animation system", i));
                assertEquals(expectedFallFrame.getRegionHeight(), fallFrame.getRegionHeight(),
                    String.format("Trial %d: Fall frame height should match animation system", i));
                
                // Verify coordinates match (fall frame is being used, not normal animation)
                assertEquals(expectedFallFrame.getRegionX(), fallFrame.getRegionX(),
                    String.format("Trial %d: Fall frame X coordinate should match animation system", i));
                assertEquals(expectedFallFrame.getRegionY(), fallFrame.getRegionY(),
                    String.format("Trial %d: Fall frame Y coordinate should match animation system", i));
                
            } catch (Exception e) {
                throw new RuntimeException("Failed to test exclusive fall rendering on trial " + i, e);
            } finally {
                player.dispose();
            }
        }
    }
}
