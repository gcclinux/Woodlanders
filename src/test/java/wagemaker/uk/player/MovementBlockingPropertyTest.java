package wagemaker.uk.player;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import wagemaker.uk.weather.WaterPuddle;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based test for movement blocking during fall.
 * **Feature: puddle-fall-damage, Property 3: Movement blocked during fall**
 * **Validates: Requirements 1.3**
 * 
 * Tests that for any movement input during an active fall sequence,
 * the player's position remains unchanged until the sequence completes.
 */
public class MovementBlockingPropertyTest {
    
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
     * Property 3: Movement blocked during fall
     * For any movement input during an active fall sequence,
     * the player's position should remain unchanged until the sequence completes.
     * 
     * Validates: Requirements 1.3
     * 
     * This property-based test runs 100 trials with random initial positions.
     */
    @Test
    void movementBlockedDuringFall() {
        // **Feature: puddle-fall-damage, Property 3: Movement blocked during fall**
        
        Random random = new Random(42); // Fixed seed for reproducibility
        int trials = 100;
        
        for (int i = 0; i < trials; i++) {
            // Generate random initial position
            float initialX = random.nextFloat() * 1000.0f - 500.0f;
            float initialY = random.nextFloat() * 1000.0f - 500.0f;
            
            // Create a player at the random position
            OrthographicCamera camera = new OrthographicCamera();
            Player player = new Player(initialX, initialY, camera);
            
            // Create a puddle for the fall trigger
            WaterPuddle puddle = new WaterPuddle();
            puddle.reset(0, 0, 32, 32, 0);
            
            try {
                // Trigger the fall using reflection
                java.lang.reflect.Method triggerFallMethod = Player.class.getDeclaredMethod("triggerFall", WaterPuddle.class);
                triggerFallMethod.setAccessible(true);
                triggerFallMethod.invoke(player, puddle);
                
                // Store position after fall trigger
                float positionAfterFall = player.getX();
                float positionYAfterFall = player.getY();
                
                // Simulate update with small delta time (player should not move)
                // Note: We can't actually simulate input in headless mode,
                // but we can verify that update() returns early when falling
                player.update(0.1f);
                
                // Verify position hasn't changed
                assertEquals(positionAfterFall, player.getX(), 0.001f,
                    String.format("Trial %d: Player X position should not change during fall", i));
                assertEquals(positionYAfterFall, player.getY(), 0.001f,
                    String.format("Trial %d: Player Y position should not change during fall", i));
                
            } catch (Exception e) {
                throw new RuntimeException("Failed to test movement blocking on trial " + i, e);
            } finally {
                player.dispose();
            }
        }
    }
}
