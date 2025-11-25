package wagemaker.uk.weather;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import wagemaker.uk.player.Player;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for fall animation rendering system.
 * Tests the complete animation sequence with correct sprite coordinates and timing.
 * 
 * Requirements tested:
 * - 2.1: Fall sprite displayed at (256, 1280)
 * - 2.2: Standup1 sprite displayed at (192, 1280) after 0.8s
 * - 2.3: Standup2 sprite displayed at (128, 1280) after 0.8s
 * - 2.4: Standup3 sprite displayed at (64, 1280) after 0.8s
 * - 2.5: Standup4 sprite displayed at (0, 1280) after 0.8s
 * - 2.6: Return to normal rendering after sequence completes
 * - 5.1: Frame dimensions are 64x64 pixels
 */
public class FallAnimationRenderingIntegrationTest {
    
    private static HeadlessApplication application;
    private static ShapeRenderer shapeRenderer;
    private static OrthographicCamera camera;
    
    @BeforeAll
    public static void setUpClass() {
        // Initialize LibGDX headless application
        HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
        application = new HeadlessApplication(new ApplicationAdapter() {}, config);
        
        // Mock GL20 for headless testing
        Gdx.gl = Mockito.mock(GL20.class);
        Gdx.gl20 = Mockito.mock(GL20.class);
        
        // Mock ShapeRenderer
        shapeRenderer = Mockito.mock(ShapeRenderer.class);
        
        camera = new OrthographicCamera(1280, 1024);
        camera.position.set(0, 0, 0);
        camera.update();
    }
    
    @AfterAll
    public static void tearDownClass() {
        if (application != null) {
            application.exit();
        }
    }
    
    @Test
    public void testFallAnimationFrameSequence() {
        // Test that fall animation progresses through correct sprite coordinates
        // Requirements: 2.1-2.6
        
        PuddleManager puddleManager = new PuddleManager(shapeRenderer);
        puddleManager.initialize();
        
        Player player = new Player(100f, 100f, camera);
        player.setPuddleManager(puddleManager);
        
        float deltaTime = 0.1f;
        
        // Spawn puddles
        for (int i = 0; i < 60; i++) {
            puddleManager.update(deltaTime, true, 1.0f, camera);
        }
        
        // Get puddle and move player to it
        List<WaterPuddle> activePuddles = puddleManager.getActivePuddles();
        WaterPuddle testPuddle = activePuddles.get(0);
        float puddleCenterX = testPuddle.getX() + testPuddle.getWidth() / 2.0f;
        float puddleCenterY = testPuddle.getY() + testPuddle.getHeight() / 2.0f;
        
        player.setPosition(puddleCenterX, puddleCenterY);
        
        // Trigger fall
        player.update(deltaTime);
        
        // Frame 1: Fall sprite (256, 1280) - should be displayed immediately
        TextureRegion frame1 = player.getCurrentFrame();
        assertNotNull(frame1, "Frame 1 should not be null");
        assertEquals(64, frame1.getRegionWidth(), "Frame width should be 64 pixels");
        assertEquals(64, frame1.getRegionHeight(), "Frame height should be 64 pixels");
        assertEquals(256, frame1.getRegionX(), "Frame 1 X coordinate should be 256");
        assertEquals(1280, frame1.getRegionY(), "Frame 1 Y coordinate should be 1280");
        
        // Update for 0.8 seconds (8 frames at 0.1s each)
        for (int i = 0; i < 8; i++) {
            player.update(deltaTime);
        }
        
        // Frame 2: Standup1 sprite (192, 1280)
        TextureRegion frame2 = player.getCurrentFrame();
        assertNotNull(frame2, "Frame 2 should not be null");
        assertEquals(64, frame2.getRegionWidth(), "Frame width should be 64 pixels");
        assertEquals(64, frame2.getRegionHeight(), "Frame height should be 64 pixels");
        assertEquals(192, frame2.getRegionX(), "Frame 2 X coordinate should be 192");
        assertEquals(1280, frame2.getRegionY(), "Frame 2 Y coordinate should be 1280");
        
        // Update for another 0.8 seconds
        for (int i = 0; i < 8; i++) {
            player.update(deltaTime);
        }
        
        // Frame 3: Standup2 sprite (128, 1280)
        TextureRegion frame3 = player.getCurrentFrame();
        assertNotNull(frame3, "Frame 3 should not be null");
        assertEquals(64, frame3.getRegionWidth(), "Frame width should be 64 pixels");
        assertEquals(64, frame3.getRegionHeight(), "Frame height should be 64 pixels");
        assertEquals(128, frame3.getRegionX(), "Frame 3 X coordinate should be 128");
        assertEquals(1280, frame3.getRegionY(), "Frame 3 Y coordinate should be 1280");
        
        // Update for another 0.8 seconds
        for (int i = 0; i < 8; i++) {
            player.update(deltaTime);
        }
        
        // Frame 4: Standup3 sprite (64, 1280)
        TextureRegion frame4 = player.getCurrentFrame();
        assertNotNull(frame4, "Frame 4 should not be null");
        assertEquals(64, frame4.getRegionWidth(), "Frame width should be 64 pixels");
        assertEquals(64, frame4.getRegionHeight(), "Frame height should be 64 pixels");
        assertEquals(64, frame4.getRegionX(), "Frame 4 X coordinate should be 64");
        assertEquals(1280, frame4.getRegionY(), "Frame 4 Y coordinate should be 1280");
        
        // Update for another 0.8 seconds
        for (int i = 0; i < 8; i++) {
            player.update(deltaTime);
        }
        
        // Frame 5: Standup4 sprite (0, 1280)
        TextureRegion frame5 = player.getCurrentFrame();
        assertNotNull(frame5, "Frame 5 should not be null");
        assertEquals(64, frame5.getRegionWidth(), "Frame width should be 64 pixels");
        assertEquals(64, frame5.getRegionHeight(), "Frame height should be 64 pixels");
        assertEquals(0, frame5.getRegionX(), "Frame 5 X coordinate should be 0");
        assertEquals(1280, frame5.getRegionY(), "Frame 5 Y coordinate should be 1280");
        
        // Update for another 0.8 seconds to complete sequence
        for (int i = 0; i < 8; i++) {
            player.update(deltaTime);
        }
        
        // After sequence completes, should return to normal animation
        // Normal idle frame should not be from fall sequence
        TextureRegion normalFrame = player.getCurrentFrame();
        assertNotNull(normalFrame, "Normal frame should not be null after sequence completes");
        
        // Normal frame should not be from fall sequence (Y != 1280)
        // Note: We can't easily verify the exact coordinates without knowing the direction,
        // but we can verify it's not null and has correct dimensions
        assertEquals(64, normalFrame.getRegionWidth(), "Normal frame width should be 64 pixels");
        assertEquals(64, normalFrame.getRegionHeight(), "Normal frame height should be 64 pixels");
    }
    
    @Test
    public void testAnimationTimingPrecision() {
        // Test that animation timing is precise with 0.8 second intervals
        // Requirements: 2.2-2.5
        
        PuddleManager puddleManager = new PuddleManager(shapeRenderer);
        puddleManager.initialize();
        
        Player player = new Player(100f, 100f, camera);
        player.setPuddleManager(puddleManager);
        
        float deltaTime = 0.1f;
        
        // Spawn puddles
        for (int i = 0; i < 60; i++) {
            puddleManager.update(deltaTime, true, 1.0f, camera);
        }
        
        // Get puddle and move player to it
        List<WaterPuddle> activePuddles = puddleManager.getActivePuddles();
        WaterPuddle testPuddle = activePuddles.get(0);
        float puddleCenterX = testPuddle.getX() + testPuddle.getWidth() / 2.0f;
        float puddleCenterY = testPuddle.getY() + testPuddle.getHeight() / 2.0f;
        
        player.setPosition(puddleCenterX, puddleCenterY);
        
        // Trigger fall
        player.update(deltaTime);
        
        // Verify frame 1 is displayed
        TextureRegion frame1 = player.getCurrentFrame();
        assertEquals(256, frame1.getRegionX(), "Should be on frame 1");
        
        // Update for 0.7 seconds (just before transition)
        for (int i = 0; i < 7; i++) {
            player.update(deltaTime);
        }
        
        // Should still be on frame 1
        TextureRegion stillFrame1 = player.getCurrentFrame();
        assertEquals(256, stillFrame1.getRegionX(), "Should still be on frame 1 at 0.7s");
        
        // Update one more frame (0.8 seconds total)
        player.update(deltaTime);
        
        // Should now be on frame 2
        TextureRegion frame2 = player.getCurrentFrame();
        assertEquals(192, frame2.getRegionX(), "Should transition to frame 2 at 0.8s");
        
        // Verify same timing for next transition
        for (int i = 0; i < 7; i++) {
            player.update(deltaTime);
        }
        
        TextureRegion stillFrame2 = player.getCurrentFrame();
        assertEquals(192, stillFrame2.getRegionX(), "Should still be on frame 2 at 1.5s");
        
        player.update(deltaTime);
        
        TextureRegion frame3 = player.getCurrentFrame();
        assertEquals(128, frame3.getRegionX(), "Should transition to frame 3 at 1.6s");
    }
    
    @Test
    public void testRenderingPositionConsistency() {
        // Test that fall frames are rendered at same position as normal frames
        // Requirements: 5.4
        
        PuddleManager puddleManager = new PuddleManager(shapeRenderer);
        puddleManager.initialize();
        
        Player player = new Player(100f, 100f, camera);
        player.setPuddleManager(puddleManager);
        
        float deltaTime = 0.1f;
        
        // Record initial position
        float initialX = player.getX();
        float initialY = player.getY();
        
        // Spawn puddles
        for (int i = 0; i < 60; i++) {
            puddleManager.update(deltaTime, true, 1.0f, camera);
        }
        
        // Get puddle and move player to it
        List<WaterPuddle> activePuddles = puddleManager.getActivePuddles();
        WaterPuddle testPuddle = activePuddles.get(0);
        float puddleCenterX = testPuddle.getX() + testPuddle.getWidth() / 2.0f;
        float puddleCenterY = testPuddle.getY() + testPuddle.getHeight() / 2.0f;
        
        player.setPosition(puddleCenterX, puddleCenterY);
        
        // Record position before fall
        float posBeforeFall = player.getX();
        
        // Trigger fall
        player.update(deltaTime);
        
        // Position should not change during fall animation
        assertEquals(posBeforeFall, player.getX(), 0.01f, 
                    "X position should not change during fall");
        
        // Update through entire animation sequence
        for (int i = 0; i < 40; i++) {
            player.update(deltaTime);
            
            // Position should remain constant throughout animation
            assertEquals(posBeforeFall, player.getX(), 0.01f, 
                        "X position should remain constant during animation");
        }
    }
    
    @Test
    public void testExclusiveFallRendering() {
        // Test that normal animations don't render during fall sequence
        // Requirements: 5.5
        
        PuddleManager puddleManager = new PuddleManager(shapeRenderer);
        puddleManager.initialize();
        
        Player player = new Player(100f, 100f, camera);
        player.setPuddleManager(puddleManager);
        
        float deltaTime = 0.1f;
        
        // Spawn puddles
        for (int i = 0; i < 60; i++) {
            puddleManager.update(deltaTime, true, 1.0f, camera);
        }
        
        // Get puddle and move player to it
        List<WaterPuddle> activePuddles = puddleManager.getActivePuddles();
        WaterPuddle testPuddle = activePuddles.get(0);
        float puddleCenterX = testPuddle.getX() + testPuddle.getWidth() / 2.0f;
        float puddleCenterY = testPuddle.getY() + testPuddle.getHeight() / 2.0f;
        
        player.setPosition(puddleCenterX, puddleCenterY);
        
        // Trigger fall
        player.update(deltaTime);
        
        // During fall sequence, all frames should be from Y=1280 (fall sequence)
        for (int i = 0; i < 40; i++) {
            TextureRegion currentFrame = player.getCurrentFrame();
            assertNotNull(currentFrame, "Frame should not be null during fall sequence");
            
            // All fall frames have Y=1280
            assertEquals(1280, currentFrame.getRegionY(), 
                        "All frames during fall sequence should have Y=1280");
            
            player.update(deltaTime);
        }
        
        // After sequence completes, should return to normal animation
        TextureRegion normalFrame = player.getCurrentFrame();
        assertNotNull(normalFrame, "Normal frame should not be null after sequence");
        
        // Normal frame should not be from fall sequence (Y != 1280)
        assertNotEquals(1280, normalFrame.getRegionY(), 
                       "Normal frame should not be from fall sequence");
    }
    
    @Test
    public void testFrameDimensionsConsistency() {
        // Test that all fall frames have consistent 64x64 dimensions
        // Requirements: 5.1
        
        PuddleManager puddleManager = new PuddleManager(shapeRenderer);
        puddleManager.initialize();
        
        Player player = new Player(100f, 100f, camera);
        player.setPuddleManager(puddleManager);
        
        float deltaTime = 0.1f;
        
        // Spawn puddles
        for (int i = 0; i < 60; i++) {
            puddleManager.update(deltaTime, true, 1.0f, camera);
        }
        
        // Get puddle and move player to it
        List<WaterPuddle> activePuddles = puddleManager.getActivePuddles();
        WaterPuddle testPuddle = activePuddles.get(0);
        float puddleCenterX = testPuddle.getX() + testPuddle.getWidth() / 2.0f;
        float puddleCenterY = testPuddle.getY() + testPuddle.getHeight() / 2.0f;
        
        player.setPosition(puddleCenterX, puddleCenterY);
        
        // Trigger fall
        player.update(deltaTime);
        
        // Check dimensions of all frames in sequence
        for (int i = 0; i < 40; i++) {
            TextureRegion currentFrame = player.getCurrentFrame();
            assertNotNull(currentFrame, "Frame should not be null");
            
            assertEquals(64, currentFrame.getRegionWidth(), 
                        "Frame width should be 64 pixels");
            assertEquals(64, currentFrame.getRegionHeight(), 
                        "Frame height should be 64 pixels");
            
            player.update(deltaTime);
        }
    }
    
    @Test
    public void testAnimationDoesNotThrowExceptions() {
        // Test that animation rendering doesn't throw exceptions
        // Requirements: 2.1-2.6
        
        PuddleManager puddleManager = new PuddleManager(shapeRenderer);
        puddleManager.initialize();
        
        Player player = new Player(100f, 100f, camera);
        player.setPuddleManager(puddleManager);
        
        float deltaTime = 0.1f;
        
        // Spawn puddles
        for (int i = 0; i < 60; i++) {
            puddleManager.update(deltaTime, true, 1.0f, camera);
        }
        
        // Get puddle and move player to it
        List<WaterPuddle> activePuddles = puddleManager.getActivePuddles();
        WaterPuddle testPuddle = activePuddles.get(0);
        float puddleCenterX = testPuddle.getX() + testPuddle.getWidth() / 2.0f;
        float puddleCenterY = testPuddle.getY() + testPuddle.getHeight() / 2.0f;
        
        player.setPosition(puddleCenterX, puddleCenterY);
        
        // Trigger fall and update through entire sequence
        assertDoesNotThrow(() -> {
            player.update(deltaTime);
            
            for (int i = 0; i < 45; i++) {
                player.update(deltaTime);
                player.getCurrentFrame(); // Get frame for rendering
            }
        }, "Animation sequence should not throw exceptions");
    }
}
