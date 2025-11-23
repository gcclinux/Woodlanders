package wagemaker.uk.client;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Property-based test for character selection persistence.
 * 
 * **Feature: character-selection-menu, Property 2: Character selection persistence round-trip**
 * **Validates: Requirements 4.1, 5.1, 5.2**
 */
public class CharacterSelectionPersistencePropertyTest {
    
    private static final String[] VALID_CHARACTERS = {
        "girl_red_start.png",
        "girl_navy_start.png",
        "boy_red_start.png",
        "boy_navy_start.png"
    };
    
    @AfterEach
    public void tearDown() {
        // Clean up config file after each test
        deleteConfigFile();
    }
    
    /**
     * Property: For any valid character sprite filename, if a user selects that character
     * and confirms the selection, then loading the PlayerConfig should return the same
     * character sprite filename.
     * 
     * This test runs 100 iterations with randomly selected character filenames.
     */
    @Test
    public void characterSelectionPersistsAcrossLoadSave() {
        Random random = new Random();
        
        // Run 100 trials as specified in the design document
        for (int trial = 0; trial < 100; trial++) {
            // Generate a random valid character filename
            String characterFilename = VALID_CHARACTERS[random.nextInt(VALID_CHARACTERS.length)];
            
            // Clean up any existing config before test
            deleteConfigFile();
            
            try {
                // Save the character selection
                PlayerConfig config = PlayerConfig.load();
                config.saveSelectedCharacter(characterFilename);
                
                // Load a fresh config instance
                PlayerConfig loadedConfig = PlayerConfig.load();
                
                // Verify the character selection persisted
                assertEquals(characterFilename, 
                            loadedConfig.getSelectedCharacter(),
                            "Character selection should persist across save/load cycles (trial " + trial + ")");
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
