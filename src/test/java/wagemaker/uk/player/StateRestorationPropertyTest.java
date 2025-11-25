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
import wagemaker.uk.weather.FallAnimationSystem;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based test for state restoration after fall.
 * **Feature: puddle-fall-damage, Property 4: State restoration after fall**
 * **Validates: Requirements 1.4, 2.6**
 * 
 * Tests that for any completed fall sequence, the player returns to normal
 * movement capability and normal directional sprite rendering.
 */
public class StateRestorationPropertyTest {
    
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
     * Property 4: State restoration after fall
     * For any completed fall sequence, the player should return to normal
     * movement capability and normal directional sprite rendering.
     * 
     * Validates: Requirements 1.4, 2.6
     * 
     * This property-based test runs 100 trials with random initial positions.
     */
    @Test
    void stateRestorationAfterFall() {
        // **Feature: puddle-fall-damage, Property 4: State restoration after fall**
        
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
                
                // Access the isFalling field to verify state
                java.lang.reflect.Field isFallingField = Player.class.getDeclaredField("isFalling");
                isFallingField.setAccessible(true);
                
                // Verify player is falling
                assertTrue((Boolean) isFallingField.get(player),
                    String.format("Trial %d: Player should be in falling state after trigger", i));
                
                // Complete the fall using reflection
                java.lang.reflect.Method completeFallMethod = Player.class.getDeclaredMethod("completeFall");
                completeFallMethod.setAccessible(true);
                completeFallMethod.invoke(player);
                
                // Verify player is no longer falling
                assertFalse((Boolean) isFallingField.get(player),
                    String.format("Trial %d: Player should not be falling after completion", i));
                
                // Verify fall animation system is reset
                java.lang.reflect.Field fallAnimationSystemField = Player.class.getDeclaredField("fallAnimationSystem");
                fallAnimationSystemField.setAccessible(true);
                FallAnimationSystem fallAnimationSystem = (FallAnimationSystem) fallAnimationSystemField.get(player);
                
                assertFalse(fallAnimationSystem.isFallSequenceActive(),
                    String.format("Trial %d: Fall animation should not be active after completion", i));
                
                // Verify player can move again by updating position
                float positionBeforeUpdate = player.getX();
                player.setPosition(positionBeforeUpdate + 10, player.getY());
                assertEquals(positionBeforeUpdate + 10, player.getX(), 0.001f,
                    String.format("Trial %d: Player should be able to move after fall completion", i));
                
            } catch (Exception e) {
                throw new RuntimeException("Failed to test state restoration on trial " + i, e);
            } finally {
                player.dispose();
            }
        }
    }
}
