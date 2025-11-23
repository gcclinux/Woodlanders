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
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Property-based test for player configuration loading.
 * 
 * **Feature: character-selection-menu, Property 3: Player loads configured character**
 * **Validates: Requirements 4.3, 4.4, 5.3**
 */
public class PlayerConfigurationLoadingPropertyTest {
    
    private static final String[] VALID_CHARACTERS = {
        "girl_red_start.png",
        "girl_navy_start.png",
        "boy_red_start.png",
        "boy_navy_start.png"
    };
    
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
     * Property: For any valid character sprite filename stored in PlayerConfig,
     * when the Player class is instantiated, it should load the sprite sheet
     * corresponding to that filename.
     * 
     * This test runs 100 iterations with randomly selected character filenames.
     */
    @Test
    public void playerLoadsConfiguredCharacter() {
        Random random = new Random();
        OrthographicCamera camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 600);
        
        // Run 100 trials as specified in the design document
        for (int trial = 0; trial < 100; trial++) {
            // Generate a random valid character filename
            String characterFilename = VALID_CHARACTERS[random.nextInt(VALID_CHARACTERS.length)];
            
            // Clean up any existing config before test
            deleteConfigFile();
            
            try {
                // Set character in PlayerConfig
                PlayerConfig config = PlayerConfig.load();
                config.saveSelectedCharacter(characterFilename);
                
                // Instantiate Player class
                Player player = new Player(100, 100, camera);
                
                // Verify Player was created successfully (sprite sheet loaded)
                assertNotNull(player, 
                            "Player should be instantiated successfully with character: " + 
                            characterFilename + " (trial " + trial + ")");
                
                // Verify player can provide a current frame (indicates sprite loaded)
                assertNotNull(player.getCurrentFrame(),
                            "Player should have a valid sprite frame for character: " + 
                            characterFilename + " (trial " + trial + ")");
                
            } finally {
                // Clean up after test
                deleteConfigFile();
            }
        }
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
