package wagemaker.uk.weather;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based test for puddle coordinate recording.
 * 
 * **Feature: puddle-fall-damage, Property 8: Puddle coordinates recorded**
 * **Validates: Requirements 3.1**
 * 
 * For any spawned puddle, the system should record and maintain its center 
 * coordinates for collision detection.
 */
public class PuddleCoordinatesPropertyTest {
    
    private static HeadlessApplication application;
    
    @BeforeAll
    public static void setUpClass() {
        // Initialize LibGDX headless application
        HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
        application = new HeadlessApplication(new ApplicationAdapter() {}, config);
        
        // Mock GL20 for headless testing
        Gdx.gl = Mockito.mock(GL20.class);
        Gdx.gl20 = Mockito.mock(GL20.class);
    }
    
    @AfterAll
    public static void tearDownClass() {
        if (application != null) {
            application.exit();
        }
    }
    
    /**
     * Property 8: Puddle coordinates recorded
     * For any spawned puddle, the system should record and maintain its center 
     * coordinates for collision detection.
     * 
     * Validates: Requirements 3.1
     * 
     * This property-based test runs 100 trials with random intensities.
     */
    @Test
    public void puddlesShouldRecordCenterCoordinates() {
        Random random = new Random(42); // Fixed seed for reproducibility
        
        // Run 100 trials
        for (int trial = 0; trial < 100; trial++) {
            // Mock ShapeRenderer since we don't need actual rendering
            ShapeRenderer shapeRenderer = Mockito.mock(ShapeRenderer.class);
            PuddleManager puddleManager = new PuddleManager(shapeRenderer);
            puddleManager.initialize();
            
            OrthographicCamera camera = new OrthographicCamera();
            camera.setToOrtho(false, 800, 600);
            camera.position.set(400, 300, 0);
            camera.update();
            
            // Generate random intensity
            float intensity = 0.1f + random.nextFloat() * 0.9f; // 0.1 to 1.0
            
            // Trigger puddle spawning by simulating rain accumulation
            float deltaTime = 0.1f;
            float totalTime = 0.0f;
            
            // Accumulate rain until puddles spawn
            while (totalTime < PuddleConfig.ACCUMULATION_THRESHOLD + 0.1f) {
                puddleManager.update(deltaTime, true, intensity, camera);
                totalTime += deltaTime;
            }
            
            // Get active puddles
            List<WaterPuddle> activePuddles = puddleManager.getActivePuddles();
            
            // Verify that all active puddles have valid coordinates
            for (WaterPuddle puddle : activePuddles) {
                // Check that coordinates are recorded (not NaN or Infinity)
                assertFalse(Float.isNaN(puddle.getX()),
                    "Puddle X coordinate should not be NaN");
                assertFalse(Float.isInfinite(puddle.getX()),
                    "Puddle X coordinate should not be Infinity");
                assertFalse(Float.isNaN(puddle.getY()),
                    "Puddle Y coordinate should not be NaN");
                assertFalse(Float.isInfinite(puddle.getY()),
                    "Puddle Y coordinate should not be Infinity");
                
                // Check that width and height are positive (needed for center calculation)
                assertTrue(puddle.getWidth() > 0.0f,
                    String.format("Puddle width should be positive, got %.2f", puddle.getWidth()));
                assertTrue(puddle.getHeight() > 0.0f,
                    String.format("Puddle height should be positive, got %.2f", puddle.getHeight()));
                
                // Calculate center coordinates
                float centerX = puddle.getX() + puddle.getWidth() / 2.0f;
                float centerY = puddle.getY() + puddle.getHeight() / 2.0f;
                
                // Verify center coordinates are valid
                assertFalse(Float.isNaN(centerX),
                    "Puddle center X should not be NaN");
                assertFalse(Float.isInfinite(centerX),
                    "Puddle center X should not be Infinity");
                assertFalse(Float.isNaN(centerY),
                    "Puddle center Y should not be NaN");
                assertFalse(Float.isInfinite(centerY),
                    "Puddle center Y should not be Infinity");
                
                // Verify puddle has a unique ID for tracking
                assertNotNull(puddle.getId(),
                    "Puddle should have a non-null ID");
                assertFalse(puddle.getId().isEmpty(),
                    "Puddle ID should not be empty");
            }
        }
    }
    
    /**
     * Property: Puddle coordinates should remain stable while active
     * For any spawned puddle, its coordinates should not change while it remains active.
     * 
     * This property-based test runs 100 trials with random intensities.
     */
    @Test
    public void puddleCoordinatesShouldRemainStableWhileActive() {
        Random random = new Random(42); // Fixed seed for reproducibility
        
        // Run 100 trials
        for (int trial = 0; trial < 100; trial++) {
            // Mock ShapeRenderer since we don't need actual rendering
            ShapeRenderer shapeRenderer = Mockito.mock(ShapeRenderer.class);
            PuddleManager puddleManager = new PuddleManager(shapeRenderer);
            puddleManager.initialize();
            
            OrthographicCamera camera = new OrthographicCamera();
            camera.setToOrtho(false, 800, 600);
            camera.position.set(400, 300, 0);
            camera.update();
            
            // Generate random intensity
            float intensity = 0.1f + random.nextFloat() * 0.9f; // 0.1 to 1.0
            
            // Spawn puddles
            float deltaTime = 0.1f;
            float totalTime = 0.0f;
            
            while (totalTime < PuddleConfig.ACCUMULATION_THRESHOLD + 0.1f) {
                puddleManager.update(deltaTime, true, intensity, camera);
                totalTime += deltaTime;
            }
            
            // Get active puddles and record their coordinates
            List<WaterPuddle> activePuddles = puddleManager.getActivePuddles();
            
            if (activePuddles.isEmpty()) {
                continue; // No puddles spawned, skip this trial
            }
            
            // Record initial coordinates
            float[] initialX = new float[activePuddles.size()];
            float[] initialY = new float[activePuddles.size()];
            
            for (int i = 0; i < activePuddles.size(); i++) {
                WaterPuddle puddle = activePuddles.get(i);
                initialX[i] = puddle.getX();
                initialY[i] = puddle.getY();
            }
            
            // Update puddles several times while rain continues
            for (int i = 0; i < 10; i++) {
                puddleManager.update(deltaTime, true, intensity, camera);
            }
            
            // Get puddles again and verify coordinates haven't changed
            List<WaterPuddle> updatedPuddles = puddleManager.getActivePuddles();
            
            assertEquals(activePuddles.size(), updatedPuddles.size(),
                "Number of active puddles should remain the same");
            
            for (int i = 0; i < updatedPuddles.size(); i++) {
                WaterPuddle puddle = updatedPuddles.get(i);
                assertEquals(initialX[i], puddle.getX(), 0.001f,
                    String.format("Puddle X coordinate should remain stable. Expected %.2f, got %.2f",
                        initialX[i], puddle.getX()));
                assertEquals(initialY[i], puddle.getY(), 0.001f,
                    String.format("Puddle Y coordinate should remain stable. Expected %.2f, got %.2f",
                        initialY[i], puddle.getY()));
            }
        }
    }
}
