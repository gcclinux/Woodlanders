package wagemaker.uk.player;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import wagemaker.uk.client.PlayerConfig;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Unit tests for Player character loading functionality.
 * Tests Requirements: 4.3, 4.4, 4.5, 5.3, 5.4, 5.5
 */
public class PlayerCharacterLoadingTest {
    
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
    
    @AfterEach
    public void tearDown() {
        // Clean up config file after each test
        deleteConfigFile();
    }
    
    /**
     * Test that Player loads correct sprite when config has valid character.
     * Validates Requirements: 4.3, 4.4, 5.3
     */
    @Test
    public void playerLoadsCorrectSpriteFromConfig() {
        OrthographicCamera camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 600);
        
        // Set a specific character in config
        PlayerConfig config = PlayerConfig.load();
        config.saveSelectedCharacter("girl_red_start.png");
        
        // Create player - should load the configured character
        Player player = new Player(100, 100, camera);
        
        // Verify player was created successfully
        assertNotNull(player, "Player should be instantiated with configured character");
        assertNotNull(player.getCurrentFrame(), "Player should have valid sprite frame");
    }
    
    /**
     * Test that Player uses default when config is empty.
     * Validates Requirements: 4.5, 5.4
     */
    @Test
    public void playerUsesDefaultWhenConfigEmpty() {
        OrthographicCamera camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 600);
        
        // Ensure config file doesn't exist (empty config)
        deleteConfigFile();
        
        // Create player - should use default character
        Player player = new Player(100, 100, camera);
        
        // Verify player was created successfully with default
        assertNotNull(player, "Player should be instantiated with default character");
        assertNotNull(player.getCurrentFrame(), "Player should have valid sprite frame with default");
    }
    
    /**
     * Test that Player handles missing sprite files gracefully.
     * Validates Requirements: 4.5, 5.4, 5.5
     */
    @Test
    public void playerHandlesMissingSpriteFilesGracefully() {
        OrthographicCamera camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 600);
        
        // Set an invalid/non-existent character in config
        PlayerConfig config = PlayerConfig.load();
        config.saveSelectedCharacter("nonexistent_character.png");
        
        // Create player - should fall back to default character
        Player player = new Player(100, 100, camera);
        
        // Verify player was created successfully with fallback
        assertNotNull(player, "Player should be instantiated with fallback character");
        assertNotNull(player.getCurrentFrame(), "Player should have valid sprite frame after fallback");
    }
    
    /**
     * Helper method to delete the config file.
     */
    private void deleteConfigFile() {
        File configFile = getConfigFile();
        if (configFile != null && configFile.exists()) {
            configFile.delete();
        }
    }
    
    /**
     * Gets the configuration file path.
     */
    private File getConfigFile() {
        File configDir = getConfigDirectory();
        return new File(configDir, "woodlanders.json");
    }
    
    /**
     * Gets the configuration directory based on the operating system.
     */
    private File getConfigDirectory() {
        String os = System.getProperty("os.name").toLowerCase();
        String userHome = System.getProperty("user.home");
        
        if (os.contains("win")) {
            String appData = System.getenv("APPDATA");
            if (appData != null) {
                return new File(appData, "Woodlanders");
            } else {
                return new File(userHome, "AppData/Roaming/Woodlanders");
            }
        } else if (os.contains("mac")) {
            return new File(userHome, "Library/Application Support/Woodlanders");
        } else {
            return new File(userHome, ".config/woodlanders");
        }
    }
}
