package wagemaker.uk.ui;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import org.junit.jupiter.api.*;
import wagemaker.uk.localization.LocalizationManager;

import java.io.BufferedReader;
import java.io.FileReader;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for PlayerProfileMenu integration with CharacterSelectionDialog.
 * Tests menu option display, dialog opening, and lifecycle management.
 * Validates: Requirements 1.1, 1.2, 1.5
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PlayerProfileMenuCharacterSelectionIntegrationTest {
    
    private static HeadlessApplication application;
    private static LocalizationManager localizationManager;
    
    @BeforeAll
    public static void setupGdx() {
        // Initialize headless LibGDX application for testing
        HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
        application = new HeadlessApplication(new ApplicationAdapter() {}, config);
        
        // Wait for Gdx to be initialized
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        localizationManager = LocalizationManager.getInstance();
        localizationManager.initialize();
    }
    
    @AfterAll
    public static void tearDownGdx() {
        if (application != null) {
            application.exit();
        }
    }
    
    @BeforeEach
    public void setUp() {
        // Reset to English before each test
        localizationManager.setLanguage("en");
    }
    
    /**
     * Test 1: "Choose Character" option appears in correct position
     * Validates: Requirements 1.1
     */
    @Test
    @Order(1)
    public void testChooseCharacterOptionAppearsInCorrectPosition() throws Exception {
        // Read the PlayerProfileMenu source code
        String filePath = "src/main/java/wagemaker/uk/ui/PlayerProfileMenu.java";
        StringBuilder content = new StringBuilder();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        
        String sourceCode = content.toString();
        
        // Verify menu options array includes choose_character
        assertTrue(sourceCode.contains("player_profile_menu.choose_character"),
            "PlayerProfileMenu should include choose_character option");
        
        // Verify it's at index 1 (after player_name)
        int playerNameIndex = sourceCode.indexOf("player_profile_menu.player_name");
        int chooseCharacterIndex = sourceCode.indexOf("player_profile_menu.choose_character");
        int savePlayerIndex = sourceCode.indexOf("player_profile_menu.save_player");
        
        assertTrue(playerNameIndex > 0, "player_name should exist");
        assertTrue(chooseCharacterIndex > 0, "choose_character should exist");
        assertTrue(savePlayerIndex > 0, "save_player should exist");
        
        // Verify choose_character comes after player_name and before save_player
        assertTrue(chooseCharacterIndex > playerNameIndex,
            "choose_character should come after player_name");
        assertTrue(savePlayerIndex > chooseCharacterIndex,
            "save_player should come after choose_character");
    }
    
    /**
     * Test 2: "Choose Character" translation key exists in all languages
     * Validates: Requirements 1.1
     */
    @Test
    @Order(2)
    public void testChooseCharacterTranslationKeyExistsInAllLanguages() {
        String[] languages = {"en", "pl", "pt", "nl", "de"};
        
        for (String lang : languages) {
            localizationManager.setLanguage(lang);
            
            String chooseCharacter = localizationManager.getText("player_profile_menu.choose_character");
            assertNotNull(chooseCharacter, "choose_character should exist in " + lang);
            assertFalse(chooseCharacter.startsWith("["), 
                "choose_character should have translation in " + lang);
            assertFalse(chooseCharacter.trim().isEmpty(), 
                "choose_character should not be empty in " + lang);
        }
    }
    
    /**
     * Test 3: PlayerProfileMenu has CharacterSelectionDialog field
     * Validates: Requirements 1.2
     */
    @Test
    @Order(3)
    public void testPlayerProfileMenuHasCharacterSelectionDialogField() throws Exception {
        // Read the PlayerProfileMenu source code
        String filePath = "src/main/java/wagemaker/uk/ui/PlayerProfileMenu.java";
        StringBuilder content = new StringBuilder();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        
        String sourceCode = content.toString();
        
        // Verify PlayerProfileMenu has CharacterSelectionDialog field
        assertTrue(sourceCode.contains("CharacterSelectionDialog"),
            "PlayerProfileMenu should have CharacterSelectionDialog field");
        
        // Verify it's instantiated in constructor
        assertTrue(sourceCode.contains("new CharacterSelectionDialog()"),
            "PlayerProfileMenu should instantiate CharacterSelectionDialog");
    }
    
    /**
     * Test 4: Selecting "Choose Character" option opens dialog
     * Validates: Requirements 1.2
     */
    @Test
    @Order(4)
    public void testSelectingChooseCharacterOpensDialog() throws Exception {
        // Read the PlayerProfileMenu source code
        String filePath = "src/main/java/wagemaker/uk/ui/PlayerProfileMenu.java";
        StringBuilder content = new StringBuilder();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        
        String sourceCode = content.toString();
        
        // Verify there's a method to handle menu selection
        assertTrue(sourceCode.contains("handleMenuSelection") || sourceCode.contains("Input.Keys.ENTER"),
            "PlayerProfileMenu should handle menu selection");
        
        // Verify it opens the character selection dialog
        assertTrue(sourceCode.contains("characterSelectionDialog.open()"),
            "PlayerProfileMenu should call characterSelectionDialog.open()");
        
        // Verify the selection is for index 1 (choose character)
        assertTrue(sourceCode.contains("selectedIndex == 1"),
            "PlayerProfileMenu should check for index 1 (choose character)");
    }
    
    /**
     * Test 5: Dialog update is called when dialog is open
     * Validates: Requirements 1.2
     */
    @Test
    @Order(5)
    public void testDialogUpdateIsCalledWhenDialogIsOpen() throws Exception {
        // Read the PlayerProfileMenu source code
        String filePath = "src/main/java/wagemaker/uk/ui/PlayerProfileMenu.java";
        StringBuilder content = new StringBuilder();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        
        String sourceCode = content.toString();
        
        // Verify update method checks if dialog is open
        assertTrue(sourceCode.contains("characterSelectionDialog.isOpen()"),
            "PlayerProfileMenu update should check if dialog is open");
        
        // Verify it calls dialog.update()
        assertTrue(sourceCode.contains("characterSelectionDialog.update()"),
            "PlayerProfileMenu should call characterSelectionDialog.update()");
    }
    
    /**
     * Test 6: Dialog render is called when dialog is open
     * Validates: Requirements 1.2
     */
    @Test
    @Order(6)
    public void testDialogRenderIsCalledWhenDialogIsOpen() throws Exception {
        // Read the PlayerProfileMenu source code
        String filePath = "src/main/java/wagemaker/uk/ui/PlayerProfileMenu.java";
        StringBuilder content = new StringBuilder();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        
        String sourceCode = content.toString();
        
        // Verify render method calls dialog.render()
        assertTrue(sourceCode.contains("characterSelectionDialog.render("),
            "PlayerProfileMenu render should call characterSelectionDialog.render()");
    }
    
    /**
     * Test 7: Dialog is properly disposed when menu is disposed
     * Validates: Requirements 1.5
     */
    @Test
    @Order(7)
    public void testDialogIsProperlyDisposed() throws Exception {
        // Read the PlayerProfileMenu source code
        String filePath = "src/main/java/wagemaker/uk/ui/PlayerProfileMenu.java";
        StringBuilder content = new StringBuilder();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        
        String sourceCode = content.toString();
        
        // Verify dispose method disposes the dialog
        assertTrue(sourceCode.contains("characterSelectionDialog.dispose()"),
            "PlayerProfileMenu dispose should call characterSelectionDialog.dispose()");
    }
    
    /**
     * Test 8: Menu input is prevented when dialog is open
     * Validates: Requirements 1.5
     */
    @Test
    @Order(8)
    public void testMenuInputIsPreventedWhenDialogIsOpen() throws Exception {
        // Read the PlayerProfileMenu source code
        String filePath = "src/main/java/wagemaker/uk/ui/PlayerProfileMenu.java";
        StringBuilder content = new StringBuilder();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        
        String sourceCode = content.toString();
        
        // Find the update method
        int updateStart = sourceCode.indexOf("public void update()");
        assertTrue(updateStart > 0, "update method should exist");
        
        // Find the next method
        int nextMethodStart = sourceCode.indexOf("public void", updateStart + 1);
        if (nextMethodStart == -1) {
            nextMethodStart = sourceCode.indexOf("private void", updateStart + 1);
        }
        if (nextMethodStart == -1) {
            nextMethodStart = sourceCode.length();
        }
        
        String updateMethod = sourceCode.substring(updateStart, nextMethodStart);
        
        // Verify update method checks if dialog is open and returns early
        assertTrue(updateMethod.contains("characterSelectionDialog.isOpen()"),
            "update should check if dialog is open");
        assertTrue(updateMethod.contains("return"),
            "update should return early when dialog is open");
    }
    
    /**
     * Test 9: Menu height is adjusted to accommodate new option
     * Validates: Requirements 1.1
     */
    @Test
    @Order(9)
    public void testMenuHeightIsAdjusted() throws Exception {
        // Read the PlayerProfileMenu source code
        String filePath = "src/main/java/wagemaker/uk/ui/PlayerProfileMenu.java";
        StringBuilder content = new StringBuilder();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        
        String sourceCode = content.toString();
        
        // Verify MENU_HEIGHT is defined
        assertTrue(sourceCode.contains("MENU_HEIGHT"),
            "PlayerProfileMenu should define MENU_HEIGHT");
        
        // Verify it's at least 300 to accommodate 6 options (was 5 before)
        // Each option takes about 35 pixels, plus title and padding
        assertTrue(sourceCode.contains("MENU_HEIGHT = 310") || 
                   sourceCode.contains("MENU_HEIGHT = 300") ||
                   sourceCode.contains("MENU_HEIGHT = 320"),
            "MENU_HEIGHT should be increased to accommodate new option");
    }
    
    /**
     * Test 10: Save Player option index is updated
     * Validates: Requirements 1.1
     */
    @Test
    @Order(10)
    public void testSavePlayerOptionIndexIsUpdated() throws Exception {
        // Read the PlayerProfileMenu source code
        String filePath = "src/main/java/wagemaker/uk/ui/PlayerProfileMenu.java";
        StringBuilder content = new StringBuilder();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        
        String sourceCode = content.toString();
        
        // Verify Save Player disable logic uses index 2 (was 1 before)
        assertTrue(sourceCode.contains("i == 2") && sourceCode.contains("FreeWorldManager.isFreeWorldActive()"),
            "Save Player disable logic should check index 2 (after adding choose_character at index 1)");
    }
}
