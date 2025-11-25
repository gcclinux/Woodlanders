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

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based test for rendering position consistency.
 * **Feature: puddle-fall-damage, Property 13: Rendering position consistency**
 * **Validates: Requirements 5.4**
 * 
 * Tests that for any fall/standup frame rendering, the sprite is rendered
 * at the same screen position as normal player sprites.
 */
public class RenderingPositionConsistencyPropertyTest {
    
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
     * Property 13: Rendering position consistency
     * For any fall/standup frame rendering, the sprite should be rendered
     * at the same screen position as normal player sprites.
     * 
     * Validates: Requirements 5.4
     * 
     * This property-based test runs 100 trials with random player positions.
     */
    @Test
    void renderingPositionConsistency() {
        // **Feature: puddle-fall-damage, Property 13: Rendering position consistency**
        
        Random random = new Random(42); // Fixed seed for reproducibility
        int trials = 100;
        
        for (int i = 0; i < trials; i++) {
            // Generate random position
            float posX = random.nextFloat() * 1000.0f - 500.0f;
            float posY = random.nextFloat() * 1000.0f - 500.0f;
            
            // Create a player at the random position
            OrthographicCamera camera = new OrthographicCamera();
            Player player = new Player(posX, posY, camera);
            
            // Create a puddle for the fall trigger
            WaterPuddle puddle = new WaterPuddle();
            puddle.reset(0, 0, 32, 32, 0);
            
            try {
                // Get normal frame before fall
                TextureRegion normalFrame = player.getCurrentFrame();
                assertNotNull(normalFrame,
                    String.format("Trial %d: Normal frame should not be null", i));
                
                // Trigger the fall using reflection
                java.lang.reflect.Method triggerFallMethod = Player.class.getDeclaredMethod("triggerFall", WaterPuddle.class);
                triggerFallMethod.setAccessible(true);
                triggerFallMethod.invoke(player, puddle);
                
                // Get fall frame
                TextureRegion fallFrame = player.getCurrentFrame();
                assertNotNull(fallFrame,
                    String.format("Trial %d: Fall frame should not be null", i));
                
                // Verify player position hasn't changed
                assertEquals(posX, player.getX(), 0.001f,
                    String.format("Trial %d: Player X position should remain consistent", i));
                assertEquals(posY, player.getY(), 0.001f,
                    String.format("Trial %d: Player Y position should remain consistent", i));
                
                // Both frames should have the same dimensions (64x64)
                assertEquals(64, normalFrame.getRegionWidth(),
                    String.format("Trial %d: Normal frame should be 64 pixels wide", i));
                assertEquals(64, normalFrame.getRegionHeight(),
                    String.format("Trial %d: Normal frame should be 64 pixels tall", i));
                assertEquals(64, fallFrame.getRegionWidth(),
                    String.format("Trial %d: Fall frame should be 64 pixels wide", i));
                assertEquals(64, fallFrame.getRegionHeight(),
                    String.format("Trial %d: Fall frame should be 64 pixels tall", i));
                
            } catch (Exception e) {
                throw new RuntimeException("Failed to test rendering position consistency on trial " + i, e);
            } finally {
                player.dispose();
            }
        }
    }
}
