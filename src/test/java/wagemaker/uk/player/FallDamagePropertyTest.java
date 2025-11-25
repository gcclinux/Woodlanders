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
 * Property-based test for fall damage consistency.
 * **Feature: puddle-fall-damage, Property 2: Fall damage is consistent**
 * **Validates: Requirements 1.2**
 * 
 * Tests that for any initial player health value, when a fall is triggered,
 * the player's health decreases by exactly 10%.
 */
public class FallDamagePropertyTest {
    
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
     * Property 2: Fall damage is consistent
     * For any initial player health value, when a fall is triggered,
     * the player's health should decrease by exactly 10%.
     * 
     * Validates: Requirements 1.2
     * 
     * This property-based test runs 100 trials with random health values.
     */
    @Test
    void fallDamageIsConsistent() {
        // **Feature: puddle-fall-damage, Property 2: Fall damage is consistent**
        
        Random random = new Random(42); // Fixed seed for reproducibility
        int trials = 100;
        
        for (int i = 0; i < trials; i++) {
            // Generate random initial health between 0 and 100
            float initialHealth = random.nextFloat() * 100.0f;
            
            // Create a player with the given initial health
            OrthographicCamera camera = new OrthographicCamera();
            Player player = new Player(0, 0, camera);
            player.setHealth(initialHealth);
            
            // Create a puddle for the fall trigger
            WaterPuddle puddle = new WaterPuddle();
            puddle.reset(0, 0, 32, 32, 0);
            
            // Trigger the fall using reflection to access private method
            try {
                java.lang.reflect.Method triggerFallMethod = Player.class.getDeclaredMethod("triggerFall", WaterPuddle.class);
                triggerFallMethod.setAccessible(true);
                triggerFallMethod.invoke(player, puddle);
                
                // Calculate expected health after 10% damage
                float expectedHealth = Math.max(0, initialHealth - 10.0f);
                
                // Verify the health decreased by exactly 10%
                assertEquals(expectedHealth, player.getHealth(), 0.001f,
                    String.format("Trial %d: Initial health %.2f should result in %.2f after fall, but got %.2f",
                        i, initialHealth, expectedHealth, player.getHealth()));
                
            } catch (Exception e) {
                throw new RuntimeException("Failed to test fall damage on trial " + i, e);
            } finally {
                player.dispose();
            }
        }
    }
}
