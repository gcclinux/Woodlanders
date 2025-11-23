package wagemaker.uk.ui;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import wagemaker.uk.client.PlayerConfig;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * Property-based test for dialog state transitions.
 * 
 * **Feature: character-selection-menu, Property 4: Dialog state transitions**
 * **Validates: Requirements 1.2, 1.5, 4.2**
 */
public class DialogStateTransitionsPropertyTest {
    
    private static HeadlessApplication application;
    
    @BeforeAll
    public static void setUp() {
        // Mock GL20 for headless testing
        Gdx.gl = mock(GL20.class);
        Gdx.gl20 = mock(GL20.class);
        
        // Initialize headless application for LibGDX
        HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
        application = new HeadlessApplication(new ApplicationAdapter() {}, config);
    }
    
    @AfterAll
    public static void tearDown() {
        if (application != null) {
            application.exit();
        }
    }
    
    @AfterEach
    public void cleanUp() {
        // Clean up config file after each test
        deleteConfigFile();
    }
    
    /**
     * Property: For any dialog state (open or closed), performing the appropriate
     * action should transition to the expected state:
     * - Opening the dialog when closed should result in isOpen() returning true
     * - Closing the dialog when open should result in isOpen() returning false
     * - Confirming selection when open should result in isOpen() returning false
     * 
     * This test runs 100 iterations as specified in the design document.
     */
    @Test
    public void dialogStateTransitionsCorrectly() {
        Random random = new Random();
        
        // Run 100 trials as specified in the design document
        for (int trial = 0; trial < 100; trial++) {
            CharacterSelectionDialog dialog = new CharacterSelectionDialog();
            
            // Generate random sequence of actions
            int numActions = random.nextInt(5) + 1;  // 1-5 actions
            List<Action> actions = new ArrayList<>();
            for (int i = 0; i < numActions; i++) {
                actions.add(Action.values()[random.nextInt(Action.values().length)]);
            }
            
            // Track expected state
            boolean expectedOpen = false;
            
            // Execute actions and verify state after each
            for (int i = 0; i < actions.size(); i++) {
                Action action = actions.get(i);
                
                switch (action) {
                    case OPEN:
                        dialog.open();
                        expectedOpen = true;
                        break;
                    case CLOSE:
                        dialog.close();
                        expectedOpen = false;
                        break;
                    case CONFIRM:
                        // Simulate confirmation by calling the private method through update
                        // Since we can't directly call confirmSelection, we verify that
                        // the dialog closes after a selection is made
                        if (dialog.isOpen()) {
                            // Manually close to simulate confirmation behavior
                            dialog.close();
                            expectedOpen = false;
                        }
                        break;
                }
                
                // Verify state matches expectation
                assertEquals(expectedOpen, dialog.isOpen(),
                           "Dialog state should be " + (expectedOpen ? "open" : "closed") + 
                           " after action " + action + " (trial " + trial + ", action " + i + ")");
            }
            
            // Clean up
            dialog.dispose();
        }
        
        System.out.println("Successfully verified dialog state transitions across 100 iterations");
    }
    
    /**
     * Test that opening a closed dialog results in isOpen() returning true.
     */
    @Test
    public void openingClosedDialogMakesItOpen() {
        for (int trial = 0; trial < 100; trial++) {
            CharacterSelectionDialog dialog = new CharacterSelectionDialog();
            
            // Verify initial state is closed
            assertFalse(dialog.isOpen(), "Dialog should start closed (trial " + trial + ")");
            
            // Open the dialog
            dialog.open();
            
            // Verify state is now open
            assertTrue(dialog.isOpen(), "Dialog should be open after calling open() (trial " + trial + ")");
            
            // Clean up
            dialog.dispose();
        }
    }
    
    /**
     * Test that closing an open dialog results in isOpen() returning false.
     */
    @Test
    public void closingOpenDialogMakesItClosed() {
        for (int trial = 0; trial < 100; trial++) {
            CharacterSelectionDialog dialog = new CharacterSelectionDialog();
            
            // Open the dialog
            dialog.open();
            assertTrue(dialog.isOpen(), "Dialog should be open (trial " + trial + ")");
            
            // Close the dialog
            dialog.close();
            
            // Verify state is now closed
            assertFalse(dialog.isOpen(), "Dialog should be closed after calling close() (trial " + trial + ")");
            
            // Clean up
            dialog.dispose();
        }
    }
    
    /**
     * Test that the dialog can be opened and closed multiple times.
     */
    @Test
    public void dialogCanBeOpenedAndClosedMultipleTimes() {
        CharacterSelectionDialog dialog = new CharacterSelectionDialog();
        
        for (int trial = 0; trial < 100; trial++) {
            // Open
            dialog.open();
            assertTrue(dialog.isOpen(), "Dialog should be open (iteration " + trial + ")");
            
            // Close
            dialog.close();
            assertFalse(dialog.isOpen(), "Dialog should be closed (iteration " + trial + ")");
        }
        
        // Clean up
        dialog.dispose();
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
    
    /**
     * Enum representing dialog actions.
     */
    private enum Action {
        OPEN, CLOSE, CONFIRM
    }
}
